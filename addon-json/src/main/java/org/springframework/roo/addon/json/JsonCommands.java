package org.springframwork.roo.addon.json;

import static org.springframework.roo.shell.OptionContexts.UPDATE_PROJECT;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Commands for addon-json
 * 
 * @author Stefan Schmidt
 * @since 1.1
 */
@Component
@Service
public class JsonCommands implements CommandMarker {

    @Reference private JsonOperations jsonOperations;

    @CliCommand(value = "json add", help = "Adds @RooJson annotation to target type")
    public void add(
            @CliOption(key = "class", mandatory = false, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The java type to apply this annotation to") 
            final JavaType target,
            @CliOption(key = "rootName", mandatory = false, help = "The root name which should be used to wrap the JSON document") 
            final String rootName,
            @CliOption(key = "deepSerialize", unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", mandatory = false, help = "Indication if deep serialization should be enabled.") 
            final boolean deep,
            @CliOption(key = "iso8601Dates", unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", mandatory = false, help = "Indication if dates should be formatted according to ISO 8601") 
            final boolean iso8601Dates, 
            @CliOption(key = "nullSerialization", mandatory = false,  unspecifiedDefaultValue = "REMOVE", specifiedDefaultValue = "REMOVE", help = "serializes the specified property if it were null") 
            final NullSerialization nullSerialization,
            @CliOption(key = "fieldNames", mandatory = true, help = "a comma-seperated list of property-names which should not be serialized") 
            final String fieldNames)
    {

        jsonOperations.annotateType(target, rootName, deep, iso8601Dates);
        
    	String[] fieldNamess = fieldNames.replace(";",",").replace(" ", "").split(",");
        try {
			jsonOperations.annotateFields(target, fieldNamess, nullSerialization);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
    }

    @CliCommand(value = "json all", help = "Adds @RooJson annotation to all types annotated with @RooJavaBean")
    public void all(
            @CliOption(key = "deepSerialize", unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", mandatory = false, help = "Indication if deep serialization should be enabled") 
            final boolean deep,
            @CliOption(key = "iso8601Dates", unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", mandatory = false, help = "Indication if dates should be formatted according to ISO 8601") 
            final boolean iso8601Dates) {
    	
        jsonOperations.annotateAll(deep, iso8601Dates);

    }
    
//    @CliCommand(value = "exempt fields", help = "Adds @JSON( transformer = NoSerializationTransformer.class) to to entered property")
//    public void exempt(
//
//        @CliOption(key = "fieldNames", mandatory = true, help = "a comma-seperated list of property-names which should not be serialized") 
//        final String fieldNames,
//        @CliOption(key = "nullSerialization", mandatory = false,  unspecifiedDefaultValue = "REMOVE", specifiedDefaultValue = "REMOVE", help = "serializes the specified property if it were null") 
//        final NullSerialization nullSerialization)
//    {
//
//    }
    
    @CliAvailabilityIndicator({ "json setup", "json add", "json all" })
    public boolean isPropertyAvailable() {
        return jsonOperations.isJsonInstallationPossible();
    }
    
//    @CliAvailabilityIndicator({ "exempt fields" })
//    private boolean wasTypeAdded()
//    {
//    	boolean ret = true;
//    	if(lastTarget == null || lastTarget.equals("*"))
//    		ret = false;
//    	return ret;
//    }
}