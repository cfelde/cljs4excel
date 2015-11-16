# cljs4excel
Run a ClojureScript REPL within Microsoft Excel.

![](https://www.cljs4excel.com/img/preview-1.gif)

This is a bootstrapped ClojureScript as shown on http://clojurescript.net/
running within an Office Add-in, which is essentially a browser running within
Microsoft Excel. Using Office.js and some JS->CLJS->JS code you can then
interact with the Excel document from ClojureScript.

## How to install locally

If you just want to try it out I'd recommend following the steps as shown on
https://www.cljs4excel.com. When you have that up and running the steps to run
with all local files would be these:

1: Take the manifest file you downloaded earlier and change the SourceLocation
so that it points to a URL you're in control of.

2: Take the files in the app folder in this project repository and place them
at the location of the URL you selected. There's no need to use https for this
URL, and using localhost is also fine.

Note that the manifest XML must always be placed on some network share for
Excel to accept it. However, this network share can simply be a shared folder
locally, as long as you stick to UNC paths (for example: \\WIN-AABBCCDDEE\Share\cljs4excel.xml).
No need for any advanced external file server. It's also technically possible
to have the SourceLocation pointing to a network share instead of a web server
URL, but I've had issues making that work.

To add the XML manifest from a shared location in Excel, please see below link:

https://msdn.microsoft.com/en-us/library/office/fp123503.aspx

Consider this as an early alpha/proof of concept release at the moment. There
are plenty of things I'd like to improve, so I'll put up a list of things
we can work on soon.

## Related links

Prepackaged cljs4excel: https://www.cljs4excel.com/.

Office Add-ins: https://msdn.microsoft.com/en-us/library/office/jj220060.aspx.

## License

Copyright © 2015 Christian Felde & Contributors

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
