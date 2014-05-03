package extensions
{
	import flare.display.TextSprite;
	import flare.vis.data.Data;
	import flare.vis.data.DataSprite;
	import flare.vis.operator.label.Labeler;
	
	import flare.animate.Transitioner;
	import flare.display.TextSprite;
	import flare.query.Expression;
	import flare.util.Property;
	import flare.vis.data.Data;
	import flare.vis.data.DataSprite;
	import flare.vis.operator.Operator;

	import flash.text.TextFieldAutoSize;
	import flash.display.Sprite;
	import flash.text.TextFormat;
	import flash.text.StyleSheet;

	/**
	 * Labeler that render to html.
	 */
	public class HtmlLabeler extends Labeler
	{
		public var width:Number = 100;
		public var height:Number = 50;
		
		private var _css:StyleSheet = new StyleSheet();

		/**
		 * Creates a new HtmlLabeler.
		 * @param source the property from which to retrieve the label text.
		 *  If this value is a string or property instance, the label text will
		 *  be pulled directly from the named property. If this value is a
		 *  Function or Expression instance, the value will be used to set the
		 *  <code>textFunction<code> property and the label text will be
		 *  determined by evaluating that function.
		 * @param group the data group to process
		 * @param format optional text formatting information for labels
		 * @param policy the label creation policy. One of LAYER (for adding a
		 *  separate label layer) or CHILD (for adding labels as children of
		 *  data objects)
		 * @param filter a Boolean-valued filter function determining which
		 *  items will be given labels
		 */
		public function HtmlLabeler(source:*, group:String=Data.NODES,
			format:TextFormat=null, filter:*=null, policy:String=CHILD)
		{
			super(source, group, format, filter, policy);
		}

		public function get css():StyleSheet { return _css; }
		public function set css(css:StyleSheet):void { _css = css; }

		/** @inheritDoc */
		protected override function getLabel(d:DataSprite,
			create:Boolean=false, visible:Boolean=true):TextSprite
		{
			var label:TextSprite = super.getLabel(d, create, visible);
			label.textField.multiline = true;
			label.textField.styleSheet = _css;
			label.textField.htmlText = getLabelText(d);
			label.textField.wordWrap = true;
			label.textField.width = width;
			label.textField.autoSize = TextFieldAutoSize.CENTER;
			return label;
		}

	} // end of class HtmlLabeler
}