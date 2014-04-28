import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "play-authenticate-usage"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      javaCore,
      javaJdbc,
      javaEbean,
      "org.json" % "json" % "20090211",
      "gov.nih.imagej" % "imagej" % "1.47",
      "net.sf.json-lib" % "json-lib" % "2.4" classifier "jdk15",
      "be.objectify"  %%  "deadbolt-java"     % "2.2.1-RC2",
      "com.feth"      %%  "play-authenticate" % "0.5.2-SNAPSHOT"
    )
    
//  Uncomment this for local development of the Play Authenticate core:
/*
    val playAuthenticate = play.Project(
      "play-authenticate", "1.0-SNAPSHOT", Seq(javaCore), path = file("modules/play-authenticate")
    ).settings(
      libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.2.5",
      libraryDependencies += "com.feth" %% "play-easymail" % "0.3-SNAPSHOT",
      libraryDependencies += "org.mindrot" % "jbcrypt" % "0.3m",
      libraryDependencies += "commons-lang" % "commons-lang" % "2.6",

      resolvers += Resolver.url("play-easymail (release)", url("http://joscha.github.com/play-easymail/repo/releases/"))(Resolver.ivyStylePatterns),
      resolvers += Resolver.url("play-easymail (snapshot)", url("http://joscha.github.com/play-easymail/repo/snapshots/"))(Resolver.ivyStylePatterns)
    )
*/

    val main = play.Project(appName, appVersion, appDependencies).settings(

      resolvers += Resolver.url("Objectify Play Repository (release)", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
      resolvers += Resolver.url("Objectify Play Repository (snapshot)", url("http://schaloner.github.com/snapshots/"))(Resolver.ivyStylePatterns),

      resolvers += Resolver.url("play-easymail (release)", url("http://joscha.github.com/play-easymail/repo/releases/"))(Resolver.ivyStylePatterns),
      resolvers += Resolver.url("play-easymail (snapshot)", url("http://joscha.github.com/play-easymail/repo/snapshots/"))(Resolver.ivyStylePatterns),

      resolvers += Resolver.url("play-authenticate (release)", url("http://joscha.github.com/play-authenticate/repo/releases/"))(Resolver.ivyStylePatterns),
      resolvers += Resolver.url("play-authenticate (snapshot)", url("http://joscha.github.com/play-authenticate/repo/snapshots/"))(Resolver.ivyStylePatterns)
    )
//  Uncomment this for local development of the Play Authenticate core:
//    .dependsOn(playAuthenticate).aggregate(playAuthenticate)

}
