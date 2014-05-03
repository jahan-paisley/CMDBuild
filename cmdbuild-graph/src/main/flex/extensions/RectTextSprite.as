package extensions {

/**
* First and last round attempt to implement a text sprite with an additional box around it.
* 
* There was no inspiration to make this something complete whatsoever since I assume that
* this feature will be manifesting in the flare core in a much more refined way at one point 
* anyway.
* 
* @author <a href="http://goosebumps4all.net">martin dudek</a>
* @version not even worth a version 
*/


import flare.display.DirtySprite;
import flare.display.RectSprite;
import flare.display.TextSprite;
import flash.geom.Rectangle;
import flash.text.TextFormat;

public class RectTextSprite extends DirtySprite {

    public var textSprite:TextSprite ;

    public var rectSprite:RectSprite=null ;

    private var _withRect:Boolean = true;

    private var _updateRectFlag:Boolean;
    private var _rectHorizontalAlignment:Number = 1;

    private var _rectVerticalAlignment:Number = 1;		

    /** Flag indicating if a recantgular rect shoudld be drawn */
    public function get withRect():Boolean { return _withRect; }
    public function set withRect(b:Boolean):void {
        _withRect = b; _updateRectFlag = b; dirty();
    }

    public function get text():String { return textSprite.text; }
    public function set text(txt:String):void {
        textSprite.text = txt;_updateRectFlag=true;dirty();			
    }

    public function get rectHorizontalAlignment():Number { return _rectHorizontalAlignment; }
    public function set rectHorizontalAlignment(ha:Number):void {
        _rectHorizontalAlignment=ha;_updateRectFlag=true;dirty();			
    }
    public function get rectVerticalAlignment():Number { return _rectVerticalAlignment; }
    public function set rectVerticalAlignment(va:Number):void {
        _rectVerticalAlignment=va;_updateRectFlag=true;dirty();			
    }

    public function RectTextSprite(txt:String = null, textFormat:TextFormat = null, textMode:int = 0, withRect:Boolean  = true,rectFillColor:Number = 0xffffffff,rectLineColor:Number=0xff0000ee,rectLineWidth:Number=1) {
        super();

        textSprite = new TextSprite(txt, textFormat, textMode);

        addChild(textSprite);

        _withRect = withRect;

        if (_withRect) {
            rectSprite = new RectSprite(0, 0, textSprite.width + 2 * _rectHorizontalAlignment, textSprite.height + 2 * _rectVerticalAlignment); 				
            rectSprite.fillColor = rectFillColor
            rectSprite.lineColor = rectLineColor;
            rectSprite.lineWidth = rectLineWidth;
            _updateRectFlag = true;
            addChildAt(rectSprite, 0);
        }

        mouseChildren = false; //no mouse interaction with the text or the rectangle - sorry
    }

    public function centerSprite():void {
        var bounds:Rectangle = getBounds(parent);
        x -= bounds.width / 2;
        y -= bounds.height / 2;
        _updateRectFlag = true;
    }

    public override function render():void
    {
        super.render();
        if (_withRect) {
            if (_updateRectFlag) { 
                _updateRect();
            }
            rectSprite.render();
        }
    }

    private function _updateRect():void {
        var bb:Rectangle = textSprite.getBounds(this);
        rectSprite.x = bb.x - _rectHorizontalAlignment;
        rectSprite.y = bb.y - _rectVerticalAlignment;
        rectSprite.w = bb.width + 2 * _rectHorizontalAlignment; 				
        rectSprite.h  = bb.height + 2 * _rectVerticalAlignment;

        _updateRectFlag = false; // not sure when this has to be set true again
    }
}

}