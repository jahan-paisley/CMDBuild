(function() {
	var TYPE_DOCUMENT_NODE = 9;
	var TYPE_TEXT_NODE = 3;

	Ext.define("CMDBuild.core.xml.XMLUtility", {
		statics: {
			xmlDOMFromString: xmlDOMFromString,
			genericExtTreeFromXMLDom: function(xmlDOM) {
				var root = getDocumentRootNode(xmlDOM);

				if (root) {
					return convertXMLNode(root);
				} else {
					return {};
				}
			},

			/**
			 * visit the document and look for the elements
			 * that match the given names. When found
			 * one of them, convert it in a map with the
			 * children element names as key and their text
			 * value as value
			 */
			fromDOMToArrayOfObjects: function(xmlDOM, //
					elementNamesToConvertAsObject, //
					/**
					 * if defined add a field to the
					 * converted object that refer to the XML node
					 * that generates the objects
					 */
					nameToReferToTheWholeDOMElemen, //
					wrapperName
				) {

				// convert array names to a map to direct access
				var names = {};
				for (var i=0, l=elementNamesToConvertAsObject.length; i<l; ++i) {
					names[elementNamesToConvertAsObject[i]] = true;
				}

				var objects = [];
				var root = getDocumentRootNode(xmlDOM);
				if (root != null) {
					converXMLToObject(root, objects, names, nameToReferToTheWholeDOMElemen, wrapperName);
				}

				return objects;
			},

			serializeToString: serializeToString,

			getNodeText: getNodeText
		}
	});

	function converXMLToObject(xmlNode, objects, names, nameToReferToTheWholeDOMElemen, wrapperName) {
		if (names[xmlNode.nodeName]) {
			var convertedObject = {};
			var children = xmlNode.childNodes;
			for (var i=0, l=children.length; i<l; ++i) {
				var child = children[i];
				convertedObject[child.nodeName] = getNodeText(child);
			}

			if (typeof nameToReferToTheWholeDOMElemen != "undefined") {
				convertedObject[nameToReferToTheWholeDOMElemen] = xmlNode;
			}

			convertedObject[wrapperName] = xmlNode.nodeName;

			objects.push(convertedObject);
		}

		var children = xmlNode.childNodes;
		for (var i=0, l=children.length; i<l; ++i) {
			converXMLToObject(children[i], objects, names, nameToReferToTheWholeDOMElemen, wrapperName);
		}
	}

	function getDocumentRootNode(xmlDOM) {
		var root = null;
		if (isDocumentNode(xmlDOM)) {
			var childNodes = xmlDOM.childNodes;
			if (childNodes && childNodes.length > 0) {

				// IE take also the xml header
				// as child node of the document.
				// So, iterate over the document child
				// and take the first node different from the xml node
				for (var i=0; i<childNodes.length; ++i) {
					var n = childNodes[i];
					if (n.nodeName != "xml") {
						root = n;
					}
				}
			}
		} else {
			root = xmlDOM;
		}

		return root;
	}

	function convertXMLNode(xmlNode) {
		var childNodes = convertChildren(xmlNode.childNodes);
		var folder = childNodes.children.length > 0;
		var text = "";

		if (isTextNode(xmlNode)) {
			text = getNodeText(xmlNode);
		} else {
			text = xmlNode.nodeName;
		}

		if (childNodes.textContent.length > 0) {
			childNodes.textContent = ": " + childNodes.textContent;
		}

		var node = {
			text: text + childNodes.textContent,
			domNode: xmlNode,
			iconCls: "cm_no_display",
			leaf: !folder
		};

		if (folder) {
			node.children = childNodes.children;
		} else {
			node.iconCls = "cmdbuild-tree-no-icon";
		}

		return node;
	}

	function convertChildren(xmlChildNodes) {
		var children = [];
		var textContent = "";
		if (xmlChildNodes) {
			for (var i=0, l=xmlChildNodes.length; i<l; ++i) {
				var xmlChild = xmlChildNodes[i];
				if (isTextNode(xmlChild)) {
					textContent += (getNodeText(xmlChild) + " ");
				} else {
					children.push(convertXMLNode(xmlChild));
				}
			}
		}

		return {
			children: children,
			textContent: textContent
		};
	}

	function serializeToString(xmlNode) {
		// IE
		if (xmlNode.xml) { 
			return xmlNode.xml;
		} else {
			return (new XMLSerializer()).serializeToString(xmlNode);
		}
	}

	function xmlDOMFromString(xmlString) {
		var dom = null;

		// IE
		if (window.ActiveXObject && typeof window.ActiveXObject != "undefined") {
			var parser = new window.ActiveXObject("Microsoft.XMLDOM");
			if (parser) {
				dom = new window.ActiveXObject("Microsoft.XMLDOM");
				dom.async = "false";
				dom.loadXML(xmlString);
			}

		// The others
		} else if (window.DOMParser 
				&& typeof window.DOMParser != "undefined") {

			var parser = new window.DOMParser();
			dom = parser.parseFromString(xmlString, "text/xml");

		} else {
			throw new Error("No XML parser found");
		}

		return dom;
	}

	function isDocumentNode(node) {
		return node.nodeType == TYPE_DOCUMENT_NODE;
	}

	function isTextNode(node) {
		return node.nodeType == TYPE_TEXT_NODE;
	}

	function getNodeText(node) {
		var text = "";
		if (node.text) {
			text = node.text;
		} else if (node.textContent) {
			text = node.textContent;
		}

		return text;
	}

})();