package de.unileipzig.authors.model;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import javax.persistence.ManyToOne;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
@NotEqual(field = "title", scope = "test1")
//@NotEqualIdee(field1 = "title", reference = "leadauthor", field2 = "name")
public class Book {

    /**
     */
    private String title;

    /**
     */
    @ManyToOne
    private Author leadauthor;
}
