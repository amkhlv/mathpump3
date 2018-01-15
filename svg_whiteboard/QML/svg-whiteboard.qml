import QtQuick 2.7
import QtQuick.Controls 2.0

ApplicationWindow {

    visible: true
    width: 400
    height: 200
    id: mainwin
    title: qsTr("SVG")

    signal putStr(string msg)
    onAfterRendering: function() {width = image.width; height = image.height;}

    Item {
        id: control
        objectName: "controlObject"
        property int dismiss: 0
        onDismissChanged: mainwin.close();
    }
    Item {
        id: st
        objectName: "stateObject"

// ███████████████████████████████████████████████████████████████
// things below this line can be changed:

        property var svgfile: qsTr("")
        onSvgfileChanged: function () { image.source = svgfile }
    }

    Image {
        id: image
        cache: false
        source: "../svgs/welcome.svg";
    }
}
