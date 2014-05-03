package extensions {

import flare.vis.data.DataList;
import flare.vis.data.EdgeSprite;

public class EdgeLabelSprite extends RectTextSprite {
    public static var EDGELABELNAME:String = "EdgeLabel";

    public function EdgeLabelSprite() {
        super();
        name = EDGELABELNAME;
    }

    public static function setTextsFromDataField(dl:DataList, fieldName:String):void {
        dl.visit(function(es:EdgeSprite):void {
            var els:EdgeLabelSprite = es.getChildByName(EdgeLabelSprite.EDGELABELNAME) as EdgeLabelSprite;
            els.textSprite.text = es.data[fieldName];
        });
    }
}

}