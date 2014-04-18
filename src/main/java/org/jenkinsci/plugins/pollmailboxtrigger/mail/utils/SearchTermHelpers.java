package org.jenkinsci.plugins.pollmailboxtrigger.mail.utils;

import javax.mail.Flags;
import javax.mail.search.*;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * A slightly nicer way of building search terms.
 *
 * @author Nick Grealy
 */
public class SearchTermHelpers {

    public static SearchTerm and(SearchTerm... terms) {
        return new AndTerm(terms);
    }

    public static SearchTerm and(List<SearchTerm> terms) {
        return new AndTerm(terms.toArray(new SearchTerm[terms.size()]));
    }

    public static SearchTerm not(SearchTerm term) {
        return new NotTerm(term);
    }

    public static SearchTerm subject(String containsCaseInsensitive) {
        return new SubjectTerm(containsCaseInsensitive);
    }

    public static SearchTerm from(String email) {
        return new FromStringTerm(email);
    }

    public static SearchTerm receivedSince(Date date) {
        return new ReceivedDateTerm(ComparisonTerm.GT, date);
    }

    public static SearchTerm flag(Flags.Flag flag) {
        return new FlagTerm(new Flags(flag), true);
    }

    /**
     * @param unit   - e.g. Calendar.HOURS
     * @param amount - negative for the past!
     * @return The past date.
     */
    public static Date relativeDate(int unit, int amount) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.add(unit, amount);
        return calendar.getTime();
    }

}
