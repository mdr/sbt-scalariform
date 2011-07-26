import sbt._
import sbt.Keys._
import sbt.Project.Setting
import scalariform.formatter.preferences.{ FormattingPreferences, IFormattingPreferences }
import scalariform.formatter.ScalaFormatter
import scalariform.parser.ScalaParserException
import java.io.File

object ScalariformPlugin {
  val formatSourceDirectories = SettingKey[Seq[File]]("format-source-directories")
  val formatSourceFilter = SettingKey[FileFilter]("format-source-filter")
  val formatSources = TaskKey[Seq[File]]("format-sources")
  val formatPreferences = SettingKey[IFormattingPreferences]("format-preferences")
  val format = TaskKey[Seq[File]]("format", "Format scala sources using scalariform")

  lazy val settings: Seq[Setting[_]] = defaultSettings ++ Seq(
    compileInputs in Compile <<= (compileInputs in Compile) dependsOn (format in Compile),
    compileInputs in Test <<= (compileInputs in Test) dependsOn (format in Test)
  )

  lazy val defaultSettings: Seq[Setting[_]] = inConfig(Compile)(formatSettings) ++ inConfig(Test)(formatSettings)

  def formatSettings = Seq(
    formatSourceDirectories <<= Seq(scalaSource).join,
    formatSourceFilter := "*.scala",
    formatSources <<= collectSourceFiles,
    formatPreferences := FormattingPreferences(),
    format <<= formatTask
  )

  def collectSourceFiles = (formatSourceDirectories, formatSourceFilter, defaultExcludes in formatSources) map {
    (dirs, filter, excludes) => dirs.descendentsExcept(filter, excludes).get
  }

  def cached(cache: File)(log: String => Unit)(update: Set[File] => Unit) = {
    FileFunction.cached(cache)(FilesInfo.hash, FilesInfo.exists) { (in, out) =>
      val files = in.modified
      Util.counted("Scala source", "", "s", files.size) foreach { count => log(count) }
      update(files)
      files
    }
  }

  def formatTask = (formatSources, formatPreferences, thisProjectRef, configuration, cacheDirectory, streams) map {
    (sources, preferences, ref, config, cacheDir, s) => {
      val label = "%s(%s)" format (Project.display(ref), config)
      val cache = cacheDir / "format"
      val logFormatting = (count: String) => s.log.info("Formatting %s %s..." format (count, label))
      val logReformatted = (count: String) => s.log.info("Reformatted %s %s" format (count, label))
      val formatting = cached(cache)(logFormatting) { files =>
        for (file <- files) {
          val contents = IO.read(file)
          val formatted = ScalaFormatter.format(contents, preferences)
          if (formatted != contents) IO.write(file, formatted)
        }
      }
      val reformatted = cached(cache)(logReformatted) { files => () }
      try {
        formatting(sources.toSet)
        reformatted(sources.toSet).toSeq // recalculate cache because we're formatting in-place
      } catch {
        case e: ScalaParserException => s.log.error("Scalariform parser error: see compile for details"); Nil
      }
    }
  }
}
