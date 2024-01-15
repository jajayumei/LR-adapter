package ampLRadapter;

//IMPLEMENT createArrayValue FUNCTIE
// MAYBE SIMPLIFY TYPES

import java.util.*;

import com.google.protobuf.ByteString;

import PluginAdapter.Api.MessageOuterClass.*;
import PluginAdapter.Api.LabelOuterClass.Label;
import PluginAdapter.Api.LabelOuterClass.Label.Parameter;
import PluginAdapter.Api.LabelOuterClass.Label.Parameter.Value;
import PluginAdapter.Api.LabelOuterClass.Label.Parameter.Value.Array;
import PluginAdapter.Api.AnnouncementOuterClass.*;
import PluginAdapter.Api.ConfigurationOuterClass.*;
import PluginAdapter.Api.LabelOuterClass.*;
import PluginAdapter.Api.LabelOuterClass.Label.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// AxiniProtobuf: helper class to construct Protobuf messages.
// This class is not yet complete, but more than enough for SmartDoor adapter.
// The namegiving for the various create* methods is subject for change.
public class AxiniProtobuf {
    private static Logger logger = LoggerFactory.getLogger(AxiniProtobuf.class);

    public static final List<Label>
        EMPTY_LABEL_LIST = Collections.<Label>emptyList();

    public static final List<Label.Parameter>
        EMPTY_PARAM_LIST = Collections.<Label.Parameter>emptyList();

    public static final List<Configuration.Item>
        EMPTY_ITEM_LIST = Collections.<Configuration.Item>emptyList();

    // Current time in nano seconds since EPOCH.
    public static long timestamp() {
        return System.currentTimeMillis() * 1000 * 1000;
    }

    // ----- Label

    // Create a stimulus Label with parameters.
    public static Label createStimulus(String name, String channel,
                                       List<Label.Parameter> params) {
        return createLabel(name, channel, LabelType.STIMULUS, params);
    }

    // Create a stimulus Label with *no* parameters.
    public static Label createStimulus(String name, String channel) {
        return createLabel(name, channel, LabelType.STIMULUS, EMPTY_PARAM_LIST);
    }

    // Create a response Label with parameters.
    public static Label createResponse(String name, String channel,
                                       List<Label.Parameter> params) {
        return createLabel(name, channel, LabelType.RESPONSE, params);
    }

    // Create a response Label with *no* parameters.
    public static Label createResponse(String name, String channel) {
        return createLabel(name, channel, LabelType.RESPONSE, EMPTY_PARAM_LIST);
    }

    // Create Protobuf Label.
    public static Label createLabel(String name, String channel, LabelType type,
                                    List<Label.Parameter> params) {
        Label.Builder builder = Label.newBuilder()
            .setType(type)
            .setLabel(name)
            .setChannel(channel);

        for (Label.Parameter param : safe_p(params))
            builder.addParameters(param);

        Label label = builder.build();
        return label;
    }
    
    // Create Protobuf Label.
    public static Label createLabel(String name, String channel, LabelType type,
    								Label.Parameter ... params) {
        Label.Builder builder = Label.newBuilder()
            .setType(type)
            .setLabel(name)
            .setChannel(channel);

        for (Label.Parameter param : params)
            builder.addParameters(param);

        Label label = builder.build();
        return label;
    }

    // Create Protobuf Label on the basis of some other Label.
    public static Label createLabel(Label label, ByteString physicalLabel,
                                    long timestamp) {
        return label.toBuilder()
            .setPhysicalLabel(physicalLabel)
            .setTimestamp(timestamp)
            .build();
    }

    // Create Protobuf Label with correlation id on the basis of some other Label.
    public static Label createLabel(Label label, ByteString physicalLabel,
                                    long timestamp, long correlationId) {
        return createLabel(label, physicalLabel, timestamp)
            .toBuilder()
            .setCorrelationId(correlationId)
            .build();
    }
    
    public static Label createLabel(String name, 
			String channel, 
			Label.LabelType labelType, 
			Map<String,String> parametersNamesAndTypes,
			Map<String,Object> values) {
		
		Label.Builder labBuilder = Label.newBuilder()
				.setLabel(name)
				.setType(labelType) ;
		
		if (channel != null)  {
			labBuilder.setChannel(channel) ;
		}

		for (Map.Entry<String,String> item : parametersNamesAndTypes.entrySet()) {
			Parameter.Value.Builder valBuilder = Parameter.Value.newBuilder();
			
			switch (item.getValue()) {
			    case "integer" : 
			    	int x = 0;
			    	if(values != null) {
			    		x = (Integer) values.get(item.getKey());
			    	}
			    	valBuilder.setInteger(x);
			    	break;
			    // DONT THINK I ACTUALLY USE THIS ONE IN THIS SPECIFIC CASE
			    case "string" : 
			    	String s = "" ;
			    	if(values != null) {
			    		s = (String) values.get(item.getKey());
			    	}
			    	valBuilder.setString(s);
			    	break;
			    case "[integer]" : 
			    	int[] a = {0} ;
			    	if(values != null) {
			    		a = (int[]) values.get(item.getKey());
			    	}
			    	Parameter.Value.Array.Builder abuilder = Parameter.Value.Array.newBuilder();
			    	for (int k=0; k<a.length; k++) {
			    		Parameter.Value elem = Parameter.Value.newBuilder()
						    	   .setInteger(a[k])
						    	   .build();
			    		abuilder.addValues(elem);
			    	}
			    	valBuilder.setArray(abuilder.build());
			    	break;
			    case "[string]" : 
			    	String[] c = {""} ;
			    	if(values != null) {
			    		c = (String[]) values.get(item.getKey());

			    	}
			    	Parameter.Value.Array.Builder c_builder = Parameter.Value.Array.newBuilder();
			    	for (int k=0; k<c.length; k++) {
			    		Parameter.Value elem = Parameter.Value.newBuilder()
						    	   .setString(c[k])
						    	   .build();
			    		c_builder.addValues(elem);
			    	}
			    	valBuilder.setArray(c_builder.build());
			    	break;
			    case "boolean":
			    	boolean b = false;
			    	if(values != null) {
			    		b = (boolean) values.get(item.getKey());
			    	}
			    	valBuilder.setBoolean(b);
			    	break;
			}
			Parameter param = createParameter(item.getKey(), valBuilder.build()); 
			labBuilder.addParameters(param);
		}
		
		return labBuilder.build();
	}
    
    public static Label createLabel2(String name, 
			String channel, 
			Label.LabelType labelType, 
			Map<String,String> parametersNamesAndTypes,
			Map<String,Object> values) {
		
		Label.Builder labBuilder = Label.newBuilder()
				.setLabel(name)
				.setType(labelType) ;
		
		if (channel != null)  {
			labBuilder.setChannel(channel) ;
		}

		for (Map.Entry<String,String> item : parametersNamesAndTypes.entrySet()) {
			
			Parameter.Value value = null;
			
			switch (item.getValue()) {
			    case "integer" : 
			    	int x = 0;
			    	if(values != null) {
			    		x = (Integer) values.get(item.getKey());
			    	}
			    	value = createIntValue(x);
			    	break;
			    case "string" : 
			    	String s = "" ;
			    	if(values != null) {
			    		s = (String) values.get(item.getKey());
			    	}
			    	value = createStringValue(s);
			    	break;
			    case "[integer]" : 
			    	int[] a = {0} ;
			    	if(values != null) {
			    		a = (int[]) values.get(item.getKey());
			    	}
			    	Parameter.Value.Array.Builder abuilder = Parameter.Value.Array.newBuilder(); // ADDED
			    	
			    	
			    	for (int k=0; k<a.length; k++) {
			    		Parameter.Value elem = createIntValue(a[k]); // ADDED
			    		abuilder.addValues(elem);
			    	}
			    	
			    	value = Parameter.Value.newBuilder().setArray(abuilder.build()).build(); // ADDED
			    	
			    	break;
			    case "[string]" : 
			    	String[] c = {""} ;

			    	if(values != null) {
			    		c = (String[]) values.get(item.getKey());

			    	}
			    	Parameter.Value.Array.Builder c_builder = Parameter.Value.Array.newBuilder(); // ADDED
			    	
			    	for (int k=0; k<c.length; k++) {
			    		Parameter.Value elem = createStringValue(c[k]);
			    		c_builder.addValues(elem);
			    	}

			    	value = Parameter.Value.newBuilder().setArray(c_builder.build()).build(); // ADDED
			    	
			    	break;
			    case "boolean":
			    	boolean b = false;
			    	if(values != null) {
			    		b = (boolean) values.get(item.getKey());
			    	}
			    	value = createBooleanValue(b);
			    	break;
			}
			Parameter param = createParameter(item.getKey(), value);

			labBuilder.addParameters(param);
		}
		
		return labBuilder.build();
	}

    // ----- Parameter and Value

    public static Label.Parameter createParameter(String name,
                                                  Label.Parameter.Value value) {
        return Label.Parameter.newBuilder()
            .setName(name)
            .setValue(value)
            .build();
    }
    
    public static Label.Parameter createIntParameter(String name, int value) {
		return Label.Parameter.newBuilder()
			.setName(name)
			.setValue(createIntValue(value))
			.build();
	}
    
    public static Label.Parameter createBooleanParameter(String name, boolean value) {
		return Label.Parameter.newBuilder()
			.setName(name)
			.setValue(createBooleanValue(value))
			.build();
	}
    
    public static Label.Parameter createStringParameter(String name, String value) {
		return Label.Parameter.newBuilder()
			.setName(name)
			.setValue(createStringValue(value))
			.build();
	}
    
    public static Label.Parameter createArrayParameter(String name, Label.Parameter.Value ... values) {
		return Label.Parameter.newBuilder()
			.setName(name)
			.setValue(createArrayValue(values))
			.build();
	}
    
    public static Label.Parameter createArrayParameter(String name, List<Label.Parameter.Value> values) {
		return Label.Parameter.newBuilder()
			.setName(name)
			.setValue(createArrayValue(values))
			.build();
	}

    public static Label.Parameter.Value createStringValue(String value) {
        return Label.Parameter.Value.newBuilder()
            .setString(value)
            .build();
    }

    public static Label.Parameter.Value createIntValue(int value) {
        return Label.Parameter.Value.newBuilder()
            .setInteger(value)
            .build();
    }

    public static Label.Parameter.Value createDecimalValue(double value) {
        return Label.Parameter.Value.newBuilder()
            .setDecimal(value)
            .build();
    }

    public static Label.Parameter.Value createBooleanValue(boolean value) {
        return Label.Parameter.Value.newBuilder()
            .setBoolean(value)
            .build();
    }

    public static Label.Parameter.Value createDateValue(long value) {
        return Label.Parameter.Value.newBuilder()
            .setDate(value)
            .build();
    }

    public static Label.Parameter.Value createTimeValue(long value) {
        return Label.Parameter.Value.newBuilder()
            .setTime(value)
            .build();
    }
    
    public static Label.Parameter.Value createArrayValue(Label.Parameter.Value ... values) {
    	if (values.length == 0) {
    		logger.info("AXINIPROTOBUF, NO VALUES GIVEN");
    	}
    	
    	Label.Parameter.Value.Array.Builder arrayBuilder = Label.Parameter.Value.Array.newBuilder();
    	
    	for (Label.Parameter.Value value : values) {
    		arrayBuilder.addValues(value);
    	}
    	
    	Label.Parameter.Value.Array array = arrayBuilder.build();
    	
    	return Label.Parameter.Value.newBuilder()
    		.mergeArray(array)
    		.build();
    }
    
    public static Label.Parameter.Value createArrayValue(List<Label.Parameter.Value> values) {
    	if (values.size() == 0) {
    		logger.info("AXINIPROTOBUF, EMPTY LIST GIVEN");
    	}
    	
    	Label.Parameter.Value.Array.Builder arrayBuilder = Label.Parameter.Value.Array.newBuilder();
    	
    	for (Label.Parameter.Value value : values) {
    		arrayBuilder.addValues(value);
    	}
    	
    	Label.Parameter.Value.Array array = arrayBuilder.build();
    	
    	return Label.Parameter.Value.newBuilder()
    		.mergeArray(array)
    		.build();
    }

    // ------ Configuration

    public static Configuration.Item createItem(String key, String description,
                                                int value) {
        return Configuration.Item.newBuilder()
            .setKey(key)
            .setDescription(description)
            .setInteger(value)
            .build();
   }

    public static Configuration.Item createItem(String key, String description,
                                                String value) {
        return Configuration.Item.newBuilder()
            .setKey(key)
            .setDescription(description)
            .setString(value)
            .build();
   }

    public static Configuration.Item createItem(String key, String description,
                                                float value) {
        return Configuration.Item.newBuilder()
            .setKey(key)
            .setDescription(description)
            .setFloat(value)
            .build();
   }

    public static Configuration.Item createItem(String key, String description,
                                                boolean value) {
        return Configuration.Item.newBuilder()
            .setKey(key)
            .setDescription(description)
            .setBoolean(value)
            .build();
   }

    public static Configuration createConfiguration(List<Configuration.Item> items) {
        Configuration.Builder builder = Configuration.newBuilder();

        for (Configuration.Item item : safe_i(items))
            builder.addItems(item);

        return builder.build();
    }

    public static String getStringFromConfig(Configuration config, String key) {
        List<Configuration.Item> items = config.getItemsList();
        for (Configuration.Item item : items)
            if (item.getKey().equals(key))
                return item.getString();

        // Instead of returning an ERROR string, we should probably throw an Exception here.
        return "ERROR: key '" + key + "' is not key of String value";
    }

    // HIER OOK
    public static long getIntegerFromConfig(Configuration config, String key) {
        List<Configuration.Item> items = config.getItemsList();
        for (Configuration.Item item : items)
            if (item.getKey().equals(key))
                return item.getInteger();

        // Instead of returning 0, we should probably throw an Exception here.
        return 0;
    }

    // ------ Message

    // Create Protobuf Message: Label.
    public static Message createMsgLabel(Label label) {
        return Message.newBuilder()
            .setLabel(label)
            .build();
    }

    // Create Protobuf Message: Ready.
    public static Message createMsgReady() {
        Message.Ready ready = Message.Ready.newBuilder().build();
        return Message.newBuilder()
            .setReady(ready)
            .build();
    }

    // Create Protobuf Message: Error.
    public static Message createMsgError(String message) {
        Message.Error error = Message.Error.newBuilder()
            .setMessage(message)
            .build();
        return Message.newBuilder()
            .setError(error)
            .build();
    }

    // Create Protobuf Message: Announcement.
    public static Message createMsgAnnouncement(String name,
            List<Label> supportedLabels, Configuration configuration) {

        Announcement.Builder builder = Announcement.newBuilder()
            .setName(name)
            .setConfiguration(configuration);

        for (Label label : safe_l(supportedLabels))
            builder.addLabels(label);

        Announcement announcement = builder.build();

        return Message.newBuilder()
            .setAnnouncement(announcement)
            .build();
    }

    // ----- private methods

    // We cannot overload the methods "safe_l" and "safe_p" to a single "safe"
    // due to the limitations of Java generics.

    private static List<Label> safe_l(List<Label> list) {
        return list == null ? EMPTY_LABEL_LIST : list;
    }

    private static List<Label.Parameter> safe_p(List<Label.Parameter> list) {
        return list == null ? EMPTY_PARAM_LIST : list;
    }

    private static List<Configuration.Item> safe_i(List<Configuration.Item> list) {
        return list == null ? EMPTY_ITEM_LIST : list;
    }
}
