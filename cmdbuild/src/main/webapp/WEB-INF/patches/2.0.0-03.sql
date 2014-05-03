-- Add UI profile attributes

CREATE OR REPLACE FUNCTION patch_200_03() RETURNS VOID AS $$

BEGIN
	PERFORM cm_create_class_attribute('Role', 'DisabledCardTabs', 'character varying[]', NULL, FALSE, FALSE, 'MODE: reserved');
	PERFORM cm_create_class_attribute('Role', 'DisabledProcessTabs', 'character varying[]', NULL, FALSE, FALSE, 'MODE: reserved');
	PERFORM cm_create_class_attribute('Role', 'HideSidePanel', 'boolean', 'FALSE', TRUE, FALSE, 'MODE: reserved');
	PERFORM cm_create_class_attribute('Role', 'FullScreenMode', 'boolean', 'FALSE', TRUE, FALSE, 'MODE: reserved');
	PERFORM cm_create_class_attribute('Role', 'SimpleHistoryModeForCard', 'boolean', 'FALSE', TRUE, FALSE, 'MODE: reserved');
	PERFORM cm_create_class_attribute('Role', 'SimpleHistoryModeForProcess', 'boolean', 'FALSE', TRUE, FALSE, 'MODE: reserved');
	PERFORM cm_create_class_attribute('Role', 'ProcessWidgetAlwaysEnabled', 'boolean', 'FALSE', TRUE, FALSE, 'MODE: reserved');
END

$$ LANGUAGE PLPGSQL;

SELECT patch_200_03();

DROP FUNCTION patch_200_03();