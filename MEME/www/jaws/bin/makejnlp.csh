#!/bin/csh -f
# Modified: BAC: 12/21/2005 -- Seibel Ticket Number: 1-719RR : URL for changing password 
#             meow.url property added to jnlp file (interacts with *.jekyll.MainFrame.java

set port=80
if ($#argv == 1) then
    set web_server = $1
else if ($#argv == 2) then
    set web_server = $1
    set port = $2
else
    echo "Usage: $0 <web server name> [<port>]"
    exit 1
endif

set dir=`dirname $0`
pushd $dir/.. >>& /dev/null
set pwd=`pwd`
set path=`basename $pwd`
set release_date=`/bin/date +%m/%d/%Y`

echo "------------------------------------------------------"
echo "Starting ... `/bin/date`"
echo "------------------------------------------------------"
echo "web_server: $web_server"
echo "pwd:        $pwd"
echo "MEME_HOME:  $MEME_HOME"
echo "JAVA_HOME:  $JAVA_HOME"
echo ""
echo "    Writing $pwd/jekyll.jnlp ... `/bin/date`"
/bin/cat <<EOF >&! jekyll.jnlp
<?xml version="1.0" encoding="utf-8"?>
<!-- JNLP File for Jekyll -->
<jnlp
  spec="1.0+"
  codebase="http://${web_server}:$port/$path/"
  href="jekyll.jnlp">
  <information>
    <title>Jekyll - MEME Editing Client</title>
    <vendor>National Library of Medicine</vendor>
    <description>Jekyll - MEME Editing Client</description>
    <description kind="short">A full-featured editing client for
       the Metathesaurus Enhancement and Maintenance Environment.</description>
    <icon href="/images/jekyll.jpg" />
  </information>
  <security>
    <all-permissions />
  </security>
  <resources>
    <j2se version="1.4.2+" initial-heap-size="16m" max-heap-size="128m" href="http://java.sun.com/products/autodl/j2se" />
    <jar href="lib/config.jar" />
<!--    <jar href="lib/patch.jar" /> -->
    <jar href="lib/jekyll.jar" main="true" />
    <jar href="lib/meme.jar" />
    <jar href="lib/utils.jar" />
    <jar href="lib/samples.jar" />
    <jar href="lib/xerces.jar" />
    <jar href="lib/jaxp.jar" />
    <property name="meme.prop.loc" value="bundles/meme.prop" />
    <property name="default.rela.source" value="MTHRELA" />
    <property name="release.date" value="$release_date" />
    <property name="meow.url" value="${web_server}:$port" />
    <property name="meme.client.languages.include" value="ENG,SPA" />
  </resources>
  <application-desc main-class="gov.nih.nlm.umls.jekyll.JekyllStart"/>
</jnlp>
EOF

echo "    Build signed jar files ... `/bin/date`"
pushd lib >& /dev/null
/bin/rm -r -f tmp
/bin/mkdir tmp
foreach f (`/bin/ls *jar | /bin/grep -v meme.jar`)
    pushd tmp >>& /dev/null
    echo "      $f"
    /bin/rm -r -f *
    /bin/cp -f ../$f .
    $JAVA_HOME/bin/jar xvf $f >& /dev/null 
    /bin/rm -f META-INF/*SF
    /bin/rm -f META-INF/*DSA
    if ($f != "config.jar") then
	  /bin/rm -f meme.prop
	  /bin/rm -f bundles/meme.prop
    endif
    /bin/rm -f $f
    $JAVA_HOME/bin/jar cvf $f * >& /dev/null
    echo umls_tuttle | $JAVA_HOME/bin/jarsigner -keystore ../../etc/myKeystore $f mth >& /dev/null
    /bin/mv $f ..
    popd >& /dev/null
end
echo umls_tuttle | $JAVA_HOME/bin/jarsigner -keystore ../etc/myKeystore meme.jar mth >& /dev/null

/bin/rm -r -f tmp
popd >& /dev/null
echo "------------------------------------------------------"
echo "Finished ... `/bin/date`"
echo "------------------------------------------------------"
popd >>& /dev/null
  
