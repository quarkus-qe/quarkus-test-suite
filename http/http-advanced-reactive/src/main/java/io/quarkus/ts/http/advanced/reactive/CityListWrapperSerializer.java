package io.quarkus.ts.http.advanced.reactive;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@XmlRootElement(name = "cityListWrapper")
public class CityListWrapperSerializer {
    private final Logger logger = LoggerFactory.getLogger(CityListWrapperSerializer.class);

    public CityListDTO fromXML(String xml) {
        if (xml == null || xml.isEmpty()) {
            return new CityListDTO();
        }

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(CityListDTO.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            CityListDTO cityListDTO = (CityListDTO) unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
            if (cityListDTO.getCityList().isEmpty()) {
                throw new IllegalArgumentException("The XML payload must contain at least one city record.");
            }
            return cityListDTO;
        } catch (JAXBException e) {
            logger.error("Error deserializing XML: {}", e.getCause());
            throw new IllegalArgumentException("Error deserializing XML", e);
        }
    }

    public String toXML(CityListDTO cityListDTO) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(CityListDTO.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter xmlWriter = new StringWriter();
            marshaller.marshal(cityListDTO, xmlWriter);
            return xmlWriter.toString();
        } catch (JAXBException e) {
            logger.error("Error serializing XML: {}", e);
            throw new IllegalArgumentException("Error serializing XML", e);
        }
    }
}
