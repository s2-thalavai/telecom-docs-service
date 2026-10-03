package com.telecom.docs;

import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.util.StringUtils;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;

/**
 * Field descriptors whose "Constraints" column (see the custom request-fields.snippet template) is derived
 * from the Bean Validation annotations of the request class, so the docs can't drift from the validation rules.
 */
public final class ConstrainedFields {

    private final ConstraintDescriptions descriptions;

    public ConstrainedFields(Class<?> input) {
        this.descriptions = new ConstraintDescriptions(input);
    }

    public FieldDescriptor withPath(String path) {
        return fieldWithPath(path).attributes(key("constraints").value(
                StringUtils.collectionToDelimitedString(descriptions.descriptionsForProperty(path), ". ")));
    }
}
