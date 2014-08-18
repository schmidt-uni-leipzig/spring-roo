package org.springframework.roo.addon.json;

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
            final boolean iso8601Dates)
    {

        jsonOperations.annotateType(target, rootName, deep, iso8601Dates);
    }

    @CliCommand(value = "json all", help = "Adds @RooJson annotation to all types annotated with @RooJavaBean")
    public void all(
            @CliOption(key = "deepSerialize", unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", mandatory = false, help = "Indication if deep serialization should be enabled") 
            final boolean deep,
            @CliOption(key = "iso8601Dates", unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", mandatory = false, help = "Indication if dates should be formatted according to ISO 8601") 
            final boolean iso8601Dates) {
    	
        jsonOperations.annotateAll(deep, iso8601Dates);

    }
    
    @CliCommand(value = "json nullSerialze", help = "define properties which should be serialized in a specific way if null")
    public void customize(
            @CliOption(key = "class", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The java type to apply this annotation to") 
            final JavaType target,
            @CliOption(key = "nullSerialization", mandatory = false,  optionContext = "REMOVE, NULL, EMPTY, DEFAULT", unspecifiedDefaultValue = "REMOVE", specifiedDefaultValue = "REMOVE", 
            	help = "define how to serialize this property when it is null (REMOVE, NULL, EMPTY or DEFAULT)") 
            final String nullSerialization,
            @CliOption(key = "defaultValues", mandatory = false,  help = "if nullSerialization = DEFAULT, default values have to be issued") 
            final String defaultValues,
            @CliOption(key = "exemptFields", mandatory = true, help = "a comma-seperated list of properties for which this null-serialization rule should applie to") 
            final String fieldNames)
    {
		if (fieldNames != null) {
			String[] fieldNamess = fieldNames.replace(";", ",").replace(" ", "").split(",");
			String[] defaultValuess = defaultValues == null ? new String[0] : defaultValues.replace(";", ",").replace(" ", "").split(",");
			
			if(nullSerialization.equals("DEFAULT"))
			{
				if(fieldNamess.length == defaultValuess.length)
				{
					try {

						jsonOperations.addAdditionalDependencies();
						jsonOperations.nullSerializationOption(target, nullSerialization, fieldNamess, defaultValuess);
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
				else
					System.out.println("provide the same number of default values (--defaultValues) as fields");
			}
			else
			{
				try {
					jsonOperations.addAdditionalDependencies();
					jsonOperations.nullSerializationOption(target, nullSerialization, fieldNamess, null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println(e.getMessage());
				}
			}
		}
    }
    
    @CliAvailabilityIndicator({ "json setup", "json add", "json all", "json nullSerialze" })
    public boolean isPropertyAvailable() {
        return jsonOperations.isJsonInstallationPossible();
    }
}