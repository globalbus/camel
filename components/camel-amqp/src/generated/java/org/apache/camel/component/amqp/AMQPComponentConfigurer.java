/* Generated by camel build tools - do NOT edit this file! */
package org.apache.camel.component.amqp;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.GeneratedPropertyConfigurer;
import org.apache.camel.component.jms.JmsComponentConfigurer;

/**
 * Generated by camel build tools - do NOT edit this file!
 */
@SuppressWarnings("unchecked")
public class AMQPComponentConfigurer extends JmsComponentConfigurer implements GeneratedPropertyConfigurer {

    @Override
    public boolean configure(CamelContext camelContext, Object obj, String name, Object value, boolean ignoreCase) {
        AMQPComponent target = (AMQPComponent) obj;
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "includeamqpannotations":
        case "includeAmqpAnnotations": target.setIncludeAmqpAnnotations(property(camelContext, boolean.class, value)); return true;
        default: return super.configure(camelContext, obj, name, value, ignoreCase);
        }
    }

}

