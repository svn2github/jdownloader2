//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team jdownloader@freenet.de
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins.optional.jdreconnectrecorder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Pattern;

import jd.config.Configuration;
import jd.http.Encoding;
import jd.parser.Regex;
import jd.utils.JDHexUtils;
import jd.utils.JDUtilities;

public class JDRRUtils {

    public static String readline(BufferedInputStream in) {
        StringBuffer data = new StringBuffer("");
        int c;
        try {
            in.mark(1);
            if (in.read() == -1)
                return null;
            else
                in.reset();
            while ((c = in.read()) >= 0) {
                if ((c == 0) || (c == 10) || (c == 13))
                    break;
                else
                    data.append((char) c);
            }
            if (c == 13) {
                in.mark(1);
                if (in.read() != 10) in.reset();
            }
        } catch (Exception e) {
        }
        return data.toString();
    }

    public static InputStream newInputStream(final ByteBuffer buf) {
        return new InputStream() {
            public synchronized int read() throws IOException {
                if (!buf.hasRemaining()) { return -1; }
                return buf.get();
            }

            public synchronized int read(byte[] bytes, int off, int len) throws IOException {
                // Read only what's left
                if (!buf.hasRemaining()) { return -1; }
                len = Math.min(len, buf.remaining());
                buf.get(bytes, off, len);
                return len;
            }
        };
    }

    public static void createStep(HashMap<String, String> headers, String postdata, Vector<String> steps) {
        if (!new Regex(headers.get(null), ".*?\\.(gif|jpg|png|bmp|ico|css|js|swf|xml).*?").matches()) {
            StringBuffer hlh = new StringBuffer();
            hlh.append("    [[[STEP]]]" + "\r\n");
            hlh.append("        [[[REQUEST]]]" + "\r\n");
            hlh.append("        " + headers.get(null) + "\r\n");
            hlh.append("        Host: %%%routerip%%%" + "\r\n");
            if (headers.containsKey("authorization")) {
                String auth = new Regex(headers.get("authorization"), "Basic (.+)").getMatch(0);
                if (auth != null) JDRR.auth = Encoding.Base64Decode(auth.trim());
                hlh.append("        Authorization: Basic %%%basicauth%%%" + "\r\n");
            }
            if (headers.get(null).contains("POST") && postdata != null) {
                hlh.append("\r\n");
                hlh.append(postdata.trim());
                hlh.append("\r\n");
            }
            hlh.append("        [[[/REQUEST]]]" + "\r\n");
            hlh.append("    [[[/STEP]]]" + "\r\n");
            steps.add(hlh.toString());
        }
    }

    public static void rewriteLocationHeader(ProxyThread instance) {
        String location = JDHexUtils.toString(new Regex(instance.buffer, Pattern.compile(JDHexUtils.getHexString("Location: ") + "(.*?)" + JDHexUtils.REGEX_HTTP_NEWLINE, Pattern.CASE_INSENSITIVE | Pattern.DOTALL)).getMatch(0));
        if (location != null) {
            if (new Regex(location, "http://(.*?)/?").getMatch(0) != null) {
                String oldlocation = location;
                JDUtilities.getLogger().severe("Rewriting Location Header");
                location = new Regex(location, "http://.*?/(.+)", Pattern.DOTALL).getMatch(0);
                if (location != null) {
                    location = "http://localhost:" + JDUtilities.getSubConfig("JDRR").getIntegerProperty(JDRR.PROPERTY_PORT, 8972) + "/" + location;
                } else {
                    location = "http://localhost:" + JDUtilities.getSubConfig("JDRR").getIntegerProperty(JDRR.PROPERTY_PORT, 8972) + "/";
                }
                instance.buffer = instance.buffer.replaceAll(JDHexUtils.getHexString("Location: " + oldlocation), JDHexUtils.getHexString("Location: " + location));
                instance.renewbuffer = true;
            }
        }
    }

    public static void rewriteHostHeader(ProxyThread instance) {
        String host = JDHexUtils.toString(new Regex(instance.buffer, Pattern.compile(JDHexUtils.getHexString("Host: ") + "(.*?)" + JDHexUtils.REGEX_HTTP_NEWLINE, Pattern.CASE_INSENSITIVE | Pattern.DOTALL)).getMatch(0));
        if (host != null) {
            if (new Regex(host, "(.*?):?").getMatch(0) != null) {
                String oldhost = host;
                JDUtilities.getLogger().severe("Rewriting Host Header");
                host = JDUtilities.getConfiguration().getStringProperty(Configuration.PARAM_HTTPSEND_IP, null);
                instance.buffer = instance.buffer.replaceAll(JDHexUtils.getHexString("Host: " + oldhost), JDHexUtils.getHexString("Host: " + host));
                instance.renewbuffer = true;
            }
        }
    }

    public static void rewriteConnectionHeader(ProxyThread instance) {
        String con = JDHexUtils.toString(new Regex(instance.buffer, Pattern.compile(JDHexUtils.getHexString("Connection: ") + "(.*?)" + JDHexUtils.REGEX_HTTP_NEWLINE, Pattern.CASE_INSENSITIVE | Pattern.DOTALL)).getMatch(0));
        if (con != null) {
            String type = new Regex(con, "(.+)").getMatch(0);
            if (type != null && !type.equalsIgnoreCase("close")) {
                String oldcon = con;
                JDUtilities.getLogger().severe("Rewriting Connection Header");
                con = "close";
                instance.buffer = instance.buffer.replaceAll(JDHexUtils.getHexString("Connection: " + oldcon), JDHexUtils.getHexString("Connection: " + con));
                instance.renewbuffer = true;
            }
        }
    }

    public static void rewriteRefererHeader(ProxyThread instance) {
        String ref = JDHexUtils.toString(new Regex(instance.buffer, Pattern.compile(JDHexUtils.getHexString("Referer: ") + "(.*?)" + JDHexUtils.REGEX_HTTP_NEWLINE, Pattern.CASE_INSENSITIVE | Pattern.DOTALL)).getMatch(0));
        if (ref != null) {
            if (new Regex(ref, "http://(.*?)/?").getMatch(0) != null) {
                JDUtilities.getLogger().severe("Rewriting Referer Header");
                String oldref = ref;
                String ref2 = new Regex(ref, "http://.*?/(.+)", Pattern.DOTALL).getMatch(0);
                if (ref2 != null) {
                    ref = "http://" + JDUtilities.getConfiguration().getStringProperty(Configuration.PARAM_HTTPSEND_IP, null) + "/" + ref2.trim();
                } else {
                    ref = "http://" + JDUtilities.getConfiguration().getStringProperty(Configuration.PARAM_HTTPSEND_IP, null);
                }
                instance.buffer = instance.buffer.replaceAll(JDHexUtils.getHexString("Referer: " + oldref), JDHexUtils.getHexString("Referer: " + ref));
                instance.renewbuffer = true;
            }
        }
    }
}
