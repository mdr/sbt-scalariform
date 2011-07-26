sbt-scalariform
===============

[Scalariform][scalariform] plugin for [sbt]. This plugin requires sbt 0.10.1.

[sbt]: https://github.com/harrah/xsbt
[scalariform]: https://github.com/mdr/scalariform


Add plugin
----------

To use the plugin in a project add the following to `project/plugins/build.sbt`:

    resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

    libraryDependencies += "com.typesafe" %% "sbt-scalariform" % "0.1"


Add settings
------------

Add the scalariform plugin settings to projects that should have their sources
formatted.

In an sbt light definition:

    seq(ScalariformPlugin.settings: _*)

In an sbt full definition:

    lazy val someProject = Project(
      id = "some-project",
      base = file("."),
      settings = Defaults.defaultSettings ++ ScalariformPlugin.settings
    )

Using `ScalariformPlugin.settings` will automatically format sources when
`compile` or `test:compile` are run.


Configure scalariform
---------------------

Configure scalariform using the `formatPreferences` setting and a scalariform
`FormattingPreferences` object. For example:

    lazy val formatSettings = ScalariformPlugin.settings ++ Seq(
      formatPreferences in Compile := formattingPreferences,
      formatPreferences in Test    := formattingPreferences
    )

    def formattingPreferences = {
      import scalariform.formatter.preferences._
      FormattingPreferences().setPreference(IndentSpaces, 3)
    }

See [scalariform] for more information about possible options.
