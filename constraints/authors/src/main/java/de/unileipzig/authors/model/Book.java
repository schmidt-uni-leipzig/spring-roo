package de.unileipzig.authors.model;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import javax.persistence.ManyToOne;
import cz.jirutka.validator.spring.SpELAssertList;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
@SpELAssertList({ @cz.jirutka.validator.spring.SpELAssert(value = "!leadauthor.equals(coAuthor)", message = "nope...") })
//@SpELAssertList({ @cz.jirutka.validator.spring.SpELAssert(value = "!title.equals(leadauthor.name)", message = "Title should not be equals to name of leadauthor!") })
public class Book {

    /**
     */
    private String title;

    /**
     */
    @ManyToOne
    private Author leadauthor;

    /**
     */
    @ManyToOne
    private Author coAuthor;
}
