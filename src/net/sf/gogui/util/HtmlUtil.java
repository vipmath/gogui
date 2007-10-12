//----------------------------------------------------------------------------
// HtmlUtil.java
//----------------------------------------------------------------------------

package net.sf.gogui.util;

import  net.sf.gogui.version.Version;

/** Static utility functions related to HTML writing. */
public final class HtmlUtil
{
    /** Escape HTML special characters in text. */
    public static String escape(String text)
    {
        int len = text.length();
        StringBuffer result = new StringBuffer(len);
        for (int i = 0; i < len; ++i)
        {
            char c = text.charAt(i);
            switch (c)
            {
            case '>':
                result.append("&gt;");
                break;
            case '<':
                result.append("&lt;");
                break;
            case '&':
                result.append("&amp;");
                break;
            case '"':
                result.append("&quot;");
                break;
            case '\'':
                result.append("&apos;");
                break;
            default:
                result.append(c);
            }
        }
        return result.toString();
    }

    /** Return a footer.
        Contains a horizontal line followed by an address element containing
        the generation (current) date, the generator (applicationName) and
        a link to the GoGui website.
    */
    public static String getFooter(String applicationName)
    {
        StringBuffer buffer = new StringBuffer(512);
        buffer.append("<hr style=\"margin-bottom:0\" size=\"1\">\n" +
                      "<p style=\"margin-top:1; margin-right:5\""
                      + " align=\"right\"><i>" +
                      "<small>Generated on ");
        buffer.append(StringUtil.getDateShort());
        buffer.append(" by ");
        buffer.append(applicationName);
        buffer.append(' ');
        buffer.append(Version.get());
        buffer.append(" (<a href=\"http://gogui.sf.net\">"
                      + "http://gogui.sf.net</a>)</small></i></p>\n");
        return buffer.toString();
    }

    /** Return meta tags for character set and generator. */
    public static String getMeta(String applicationName)
    {
        String charset = StringUtil.getDefaultEncoding();
        StringBuffer buffer = new StringBuffer(512);
        buffer.append("<meta http-equiv=\"Content-Type\""
                      + " content=\"text/html; charset=");
        buffer.append(charset);
        buffer.append("\">\n" +
                      "<meta name=\"generator\" content=\"");
        buffer.append(applicationName);
        buffer.append(' ');
        buffer.append(Version.get());
        buffer.append(" (http://gogui.sf.net)\">\n");
        return buffer.toString();
    }

    /** Make constructor unavailable; class is for namespace only. */
    private HtmlUtil()
    {
    }
}
