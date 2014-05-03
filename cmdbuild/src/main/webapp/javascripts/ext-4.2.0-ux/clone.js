/**
 * http://www.extjs.com/forum/showthread.php?t=38146
 */

Ext.ux.clone = function(obj) {
	if (obj == null || typeof (obj) != 'object')
		return obj;
	if (Ext.isDate(obj))
		return obj.clone();

	var cloneArray = function(arr) {
		var len = arr.length;
		var out = [];
		if (len > 0) {
			for ( var i = 0; i < len; ++i)
				out[i] = Ext.ux.clone(arr[i]);
		}
		return out;

	};

	var c = new obj.constructor();
	for ( var prop in obj) {
		var p = obj[prop];
		if (Ext.isArray(p))
			c[prop] = cloneArray(p);
		else if (typeof p == 'object')
			c[prop] = Ext.ux.clone(p);
		else
			c[prop] = p;
	}
	return c;
};