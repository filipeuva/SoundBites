SoundBites
==========

SoundBites, environment recognition through sounds. This is an application which puts my research into audio environment recognition into practice. It was developed with the Nexus One (running Android 2.3.3) as a test unit, so support on other Android phones is not guaranteed.

(Theoretically) Works in counterpart with SoundBitesMonkey, an Android service: http://github.com/williamberg/SoundBitesMonkey

My project report (which partially explains this code) is available at: http://bio-gen.co.uk/res/Project_report.pdf

Libs
----

Modified versions of some jAudio code for audio feature calculation is included in <pre>uk.co.biogen.SoundBites.analysis.jAudio</pre>.

Required JARs:

* The <pre>marytts</pre> package v5.0, available with MaryTTS client or server. Simply include <pre>marytts-client-5.0-jar-with-dependencies.jar</pre> or <pre>marytts-client-5.0-jar-with-dependencies.jar</pre>, on the project classpath. These JARs are available at: https://github.com/marytts/marytts/wiki/MARY-TTS-5.0
* Jahmm v0.6.1, available at: http://www.run.montefiore.ulg.ac.be/~francois/software/jahmm/#downloads
