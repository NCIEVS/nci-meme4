//
//  This is one handler for dealing with IFRAME errors reported
//  by the IFrameErrorPage.  To use this code in a .jsp page
//  just import the javascript like this:
//
//    <script src="../IFrameErrorHandler.js">
//      alert("Error handler javascript page not found");
//    </script>
//

//
// This variable tracks the open error windows
//
var err_wins = new Object();

//
// The core function of error handling, it
// receives the error message, stack trace, and name of
// the iframe that generated the error
//
// This handler confirms that a window with this error
// is not opened yet, and then opens a pop-up
//
function handleError(message,trace,iframe) {
  if(!err_wins[message]) {
    msgWindow=launchCenter("" , "" ,400, 540)
    doc = msgWindow.document;
    doc.clear();
    doc.writeln('<HTML><HEAD><TITLE>Error Page</TITLE>');
    doc.writeln('<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">');
    doc.writeln('</HEAD>');
    doc.writeln('<BODY onload="document.form.submit()">');
    doc.writeln('<FORM name ="form" method="post" action="controller">');
    doc.writeln('<input type="hidden" name="state" value="IFrameErrorHandler">');
    doc.writeln('<input type="hidden" name="message" value="' + message + '">');
    doc.writeln('<input type="hidden" name="trace" value="' + trace + '">');
    doc.writeln('<input type="hidden" name="iframe" value="' + iframe + '">');
    doc.writeln('</FORM>');
    doc.writeln('</BODY>');
    doc.writeln('</HTML>');
    doc.close();
    err_wins[message] = 1;
  }
}

//
// Helper function for opening a window
//
function launchCenter(url, name, height, width)
{
	var str = "height=" + height + ",innerHeight=" + height;
	str += ",width=" + width + ",innerWidth=" + width;
	if (window.screen)
	{
		var ah = screen.availHeight - 30;
		var aw = screen.availWidth - 10;

		var xc = (aw - width) / 2;
		var yc = (ah - height) / 2;

		str += ",left=" + xc + ",screenX=" + xc;
		str += ",top=" + yc + ",screenY=" + yc;
	}
	str += ", menubar=no, scrollbars=yes, status=0, location=0, directories=0, resizable=1";
	return window.open(url, name, str);
}

//
// This function is called when the error handler
// returns, informing the application that a user has responded
// to the error message.
//
function errorhandled(iframe_name, title)
{
  err_wins[title] = null;
}
