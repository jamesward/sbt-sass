sys.props.get("plugin.version") match {
  case Some(v) => addSbtPlugin("com.jamesward" % "sbt-sass" % v)
  case _       => sys.error("'plugin.version' not set")
}
