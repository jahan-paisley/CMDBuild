/*
Copyright (c) 2008 Ryan Phelan
    http://www.rphelan.com

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package extensions
{
    
import flash.display.Graphics;

import mx.containers.Canvas;
import mx.controls.Image;
import mx.core.UIComponent;

/**
 *     LoadCanvas displays a loading indicator when isLoading is set to true
 */
public class LoadCanvas extends Canvas
{
    
    //--------------------------------------------------------------------------
    //
    //  Constructor
    //
    //--------------------------------------------------------------------------
    
    /**
     *  Constructor.
     */
    public function LoadCanvas()
    {
        super();
    }
    
    //--------------------------------------------------------------------------
    //
    //  Variables
    //
    //--------------------------------------------------------------------------

    /**
     *     @private
     *     Image that is displayed when loading
     */
    private var _loadImage:Image;
    
    /**
     *     @private
     *     Sits on top of the contents when loading
     *     to create a disabled look
     */
    private var _fade:UIComponent;
    
    //--------------------------------------------------------------------------
    //
    //  Properties
    //
    //--------------------------------------------------------------------------
    
    /**
     *     Set isLoading to true when contents of this Container are 
     *     waiting for an update
     */
    private var _isLoading:Boolean;
    
    [Bindable]
    public function set isLoading( l:Boolean ):void
    {
        _isLoading = l;
        
        invalidateDisplayList();
    }        
    public function get isLoading():Boolean 
    {
        return _isLoading;
    }
    
    /**
     *     Source path/class for the loadImage
     */
    private var _loadImageSource:Object;
    
    [Bindable]
    public function set loadImageSource( obj:Object ):void
    {
        _loadImageSource = obj;
        
        invalidateDisplayList();
    }        
    public function get loadImageSource():Object 
    {
        return _loadImageSource;
    }
    
    
    //--------------------------------------------------------------------------
    //
    //  Overridden methods
    //
    //--------------------------------------------------------------------------
    
    /**
     *     Create the loadImage and fade graphic
     */
    protected override function createChildren():void
    {
        super.createChildren();
        
        if( !_loadImage )
        {
            _loadImage = new Image();
        }
        
        if( !_fade )
        {
            _fade = new UIComponent();
        }
    }
    
    /**
     *     Update the size and position of the fade graphic and loadImage
     */
    protected override function updateDisplayList( unscaledWidth:Number, unscaledHeight:Number ):void
    {
        super.updateDisplayList( unscaledWidth, unscaledHeight );
        
        // center the _loadImage
        if( _loadImage )
        {
            _loadImage.x = unscaledWidth/2 - _loadImage.width/2;
            _loadImage.y = unscaledHeight/2 - _loadImage.height/2;
        }
        
        if( _isLoading && _loadImageSource )
        {
            _loadImage.source = _loadImageSource;
            
            // add the fade and loadImage to the display list
            if( !this.contains( _fade ) )
                addChild( _fade );
            if( !this.contains( _loadImage ) )
                addChild( _loadImage );
            
            // fill the fade component with a translucent white
            var g:Graphics = _fade.graphics;                
            g.clear();
            g.beginFill( 0xFFFFFF, .6 );
            g.drawRect( 0, 0, unscaledWidth, unscaledHeight );
            g.endFill();            
        }
        else
        {
            _loadImage.source = null;
            
            // remove the fade and load image from the display list
            if( this.contains( _fade ) )
                removeChild( _fade );
            if( this.contains( _loadImage ) )
                removeChild( _loadImage );                
        }
    }
    
}
}