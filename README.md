# cljs4excel
Run a ClojureScript REPL within Microsoft Excel.

![](https://www.cljs4excel.com/img/preview-1.gif)

This is a bootstrapped ClojureScript as shown on http://clojurescript.io/
running within an Office Add-in, which is essentially a browser running within
Microsoft Excel. Using Office.js and some JS->CLJS->JS code you can then
interact with the Excel document from ClojureScript.

## How to install locally

If you just want to try it out I'd recommend following the steps as shown on
https://www.cljs4excel.com. When you have that up and running the steps to run
with all local files would be these:

1: Build the project with:

    lein cljsbuild once
    lein resource

2: Take the manifest file in target/cljs4excel.xml and change the SourceLocation
so that it points to a URL you're in control of.

3: Take the files and folders in the target folder and place them at the location
of the URL you selected. There's no need to use https for this URL, and using
localhost is also fine.

Note that the manifest XML must always be placed on some network share for
Excel to accept it. However, this network share can simply be a shared folder
locally, as long as you stick to UNC paths (for example: \\\\WIN-AABBCCDDEE\Share\cljs4excel.xml).
No need for any advanced external file server. It's also technically possible
to have the SourceLocation pointing to a network share instead of a web server
URL, but I've had issues making that work.

To add the XML manifest from a shared location in Excel, please see below link:

https://msdn.microsoft.com/en-us/library/office/fp123503.aspx

Consider this as beta software at the moment. There are plenty of things I'd
like to improve, so I'll put up a list of things we can work on soon.

## Change log

v0.1:
* Simple proof of concept

v0.2:
* Changed to a proper leiningen project
* Use replumb for REPL
* Added scratchpad editor, open it with (show-sp) in REPL
* Included replumb work-around for (require ..)
* Added (boot ..) function, used to eval content within sheet document

v0.2.1:
* Added "debug" REPL for running in browser outside Excel (repl-debug.html)
* Added some flexibility on how get-binding-data, get-binding-details, set-binding-data! operate on arguments
* Added a fill-binding-data! function that takes any sequence and fills it into the shape of a binding.


## Related links

Prepackaged cljs4excel: https://www.cljs4excel.com/.

Office Add-ins: https://msdn.microsoft.com/en-us/library/office/jj220060.aspx.

## License

Copyright © 2015 Christian Felde & Contributors

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
