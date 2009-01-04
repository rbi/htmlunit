/*
 * Copyright (c) 2002-2009 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gargoylesoftware.htmlunit.javascript.host;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.util.DateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.BrowserRunner;
import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebTestCase;
import com.gargoylesoftware.htmlunit.BrowserRunner.Alerts;
import com.gargoylesoftware.htmlunit.BrowserRunner.Browser;
import com.gargoylesoftware.htmlunit.BrowserRunner.Browsers;
import com.gargoylesoftware.htmlunit.BrowserRunner.NotYetImplemented;
import com.gargoylesoftware.htmlunit.html.ClickableElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInlineFrame;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * Tests for {@link Document}.
 *
 * @version $Revision$
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author David K. Taylor
 * @author Barnaby Court
 * @author Chris Erskine
 * @author Marc Guillemot
 * @author Michael Ottati
 * @author <a href="mailto:george@murnock.com">George Murnock</a>
 * @author Ahmed Ashour
 * @author Rob Di Marco
 * @author Sudhan Moghe
 */
@RunWith(BrowserRunner.class)
public class DocumentTest extends WebTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "2", "form1", "form2" })
    public void formsAccessor_TwoForms() throws Exception {
        final String html
            = "<html><head><title>foo</title><script>\n"
            + "function doTest() {\n"
            + "    alert(document.forms.length)\n"
            + "    for(var i=0; i< document.forms.length; i++) {\n"
            + "        alert(document.forms[i].name )\n"
            + "    }\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<p>hello world</p>\n"
            + "<form name='form1'>\n"
            + "    <input type='text' name='textfield1' value='foo' />\n"
            + "</form>\n"
            + "<form name='form2'>\n"
            + "    <input type='text' name='textfield2' value='foo' />\n"
            + "</form>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Previously, forms with no names were not being returned by document.forms.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "1" })
    public void formsAccessor_FormWithNoName() throws Exception {
        final String html
            = "<html><head><title>foo</title><script>\n"
            + "function doTest(){\n"
            + "    alert(document.forms.length)\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<p>hello world</p>\n"
            + "<form>\n"
            + "    <input type='text' name='textfield1' value='foo' />\n"
            + "</form>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "0" })
    public void formsAccessor_NoForms() throws Exception {
        final String html
            = "<html><head><title>foo</title><script>\n"
            + "function doTest(){\n"
            + "    alert(document.forms.length)\n"
            + "    for(var i=0; i< document.forms.length; i++) {\n"
            + "        alert(document.forms[i].name )\n"
            + "    }\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<p>hello world</p>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void formArray() throws Exception {
        final WebClient client = getWebClient();
        final MockWebConnection webConnection = new MockWebConnection();

        final String firstHtml
            = "<html><head><SCRIPT lang='JavaScript'>\n"
            + "    function doSubmit(formName){\n"
            + "        var form = document.forms[formName];\n" // This line used to blow up
            + "        form.submit()\n"
            + "}\n"
            + "</SCRIPT></head><body><form name='formName' method='POST' "
            + "action='" + URL_SECOND + "'>\n"
            + "<a href='.' id='testJavascript' name='testJavascript' "
            + "onclick=\" doSubmit('formName');return false;\">\n"
            + "Test Link </a><input type='submit' value='Login' "
            + "name='loginButton'></form>\n"
            + "</body></html> ";
        final String secondHtml
            = "<html><head><title>second</title></head><body>\n"
            + "<p>hello world</p>\n"
            + "</body></html>";

        webConnection.setResponse(URL_FIRST, firstHtml);
        webConnection.setResponse(URL_SECOND, secondHtml);
        client.setWebConnection(webConnection);

        final HtmlPage page = client.getPage(URL_FIRST);
        assertEquals("", page.getTitleText());

        final HtmlAnchor testAnchor = page.getAnchorByName("testJavascript");
        final HtmlPage secondPage = (HtmlPage) testAnchor.click();
        assertEquals("second", secondPage.getTitleText());
    }

    /**
     * Test that forms is a live collection.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "0", "1", "1", "true" })
    public void formsLive() throws Exception {
        final String html =
            "<html>\n"
            + "<head>\n"
            + "<script>\n"
            + "var oCol = document.forms;\n"
            + "alert(oCol.length);\n"
            + "function test()\n"
            + "{\n"
            + "    alert(oCol.length);\n"
            + "    alert(document.forms.length);\n"
            + "    alert(document.forms == oCol);\n"
            + "}\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "<form id='myForm' action='foo.html'>\n"
            + "</form>\n"
            + "</body>\n"
            + "</html>";

        loadPageWithAlerts(html);
    }

    /**
     * Tests for <tt>document.anchors</tt>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(IE = { "0", "3", "3", "true", "id: firstLink" },
            FF = { "0", "1", "1", "true", "name: end" })
    public void anchors() throws Exception {
        final String html =
            "<html>\n"
            + "<head>\n"
            + "<script>\n"
            + "var oCol = document.anchors;\n"
            + "alert(oCol.length);\n"
            + "function test()\n"
            + "{\n"
            + "    alert(oCol.length);\n"
            + "    alert(document.anchors.length);\n"
            + "    alert(document.anchors == oCol);\n"
            + "    if (document.anchors[0].name)\n"
            + "     alert('name: ' + document.anchors[0].name);\n"
            + "    else\n"
            + "     alert('id: ' + document.anchors[0].id);\n"
            + "}\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "<a href='foo.html' id='firstLink'>foo</a>\n"
            + "<a href='foo2.html'>foo2</a>\n"
            + "<a name='end'/>\n"
            + "<a href=''>null2</a>\n"
            + "<a id='endId'/>\n"
            + "</body>\n"
            + "</html>";

        loadPageWithAlerts(html);
    }

    /**
     * Tests for <tt>document.links</tt>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "0", "3", "3", "true", "firstLink" })
    public void links() throws Exception {
        final String html =
            "<html>\n"
            + "<head>\n"
            + "<script>\n"
            + "var oCol = document.links;\n"
            + "alert(oCol.length);\n"
            + "function test()\n"
            + "{\n"
            + "    alert(oCol.length);\n"
            + "    alert(document.links.length);\n"
            + "    alert(document.links == oCol);\n"
            + "    alert(document.links[0].id);\n"
            + "}\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "<a href='foo.html' id='firstLink'>foo</a>\n"
            + "<a href='foo2.html'>foo2</a>\n"
            + "<a name='end'/>\n"
            + "<a href=''>null2</a>\n"
            + "</body>\n"
            + "</html>";

        loadPageWithAlerts(html);
    }

    /**
     * Ensures that <tt>document.createElement()</tt> works correctly.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "parentNode: null", "DIV", "1", "null", "DIV", "button1value", "text1value", "text" })
    public void createElement() throws Exception {
        final String html
            = "<html>\n"
            + "  <head>\n"
            + "    <title>First</title>\n"
            + "    <script>\n"
            + "      function doTest() {\n"
            + "        // Create a DIV element.\n"
            + "        var div1 = document.createElement('div');\n"
            + "        alert('parentNode: ' + div1.parentNode);\n"
            + "        div1.id = 'div1';\n"
            + "        document.body.appendChild(div1);\n"
            + "        alert(div1.tagName);\n"
            + "        alert(div1.nodeType);\n"
            + "        alert(div1.nodeValue);\n"
            + "        alert(div1.nodeName);\n"
            + "        // Create an INPUT element.\n"
            + "        var input = document.createElement('input');\n"
            + "        input.id = 'text1id';\n"
            + "        input.name = 'text1name';\n"
            + "        input.value = 'text1value';\n"
            + "        var form = document.getElementById('form1');\n"
            + "        form.appendChild(input);\n"
            + "        alert(document.getElementById('button1id').value);\n"
            + "        alert(document.getElementById('text1id').value);\n"
            + "        // The default type of an INPUT element is 'text'.\n"
            + "        alert(document.getElementById('text1id').type);\n"
            + "      }\n"
            + "    </script>\n"
            + "  </head>\n"
            + "  <body onload='doTest()'>\n"
            + "    <form name='form1' id='form1'>\n"
            + "      <input type='button' id='button1id' name='button1name' value='button1value'/>\n"
            + "      This is form1.\n"
            + "    </form>\n"
            + "  </body>\n"
            + "</html>";

        final HtmlPage page = loadPageWithAlerts(html);

        final HtmlElement div1 = page.getHtmlElementById("div1");
        assertEquals("div", div1.getTagName());
        assertEquals((short) 1, div1.getNodeType());
        assertEquals(null, div1.getNodeValue());
        assertEquals("div", div1.getNodeName());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(IE = { "DIV,DIV,undefined,undefined,undefined", "HI:DIV,HI:DIV,undefined,undefined,undefined" },
            FF = { "DIV,DIV,null,null,DIV", "HI:DIV,HI:DIV,null,null,HI:DIV" })
    public void documentCreateElement2() throws Exception {
        final String html
            = "<html>\n"
            + "  <head>\n"
            + "    <script>\n"
            + "      function doTest() {\n"
            + "        div = document.createElement('Div');\n"
            + "        alert(div.nodeName + ',' + div.tagName + ',' + div.namespaceURI + ',' + "
            +   "div.prefix + ',' + div.localName);\n"
            + "        div = document.createElement('Hi:Div');\n"
            + "        alert(div.nodeName + ',' + div.tagName + ',' + div.namespaceURI + ',' + "
            +   "div.prefix + ',' + div.localName);\n"
            + "      }\n"
            + "    </script>\n"
            + "  </head>\n"
            + "  <body onload='doTest()'>\n"
            + "  </body>\n"
            + "</html>";

        loadPageWithAlerts(html);
    }

    /**
     * Ensures that <tt>document.createElementNS()</tt> works correctly.
     * @throws Exception if the test fails
     */
    @Test
    @Browsers(Browser.FF)
    @Alerts(FF = { "Some:Div,Some:Div,myNS,Some,Div" })
    public void createElementNS() throws Exception {
        final String html
            = "<html>\n"
            + "  <head>\n"
            + "    <script>\n"
            + "      function doTest() {\n"
            + "        div = document.createElementNS('myNS', 'Some:Div');\n"
            + "        alert(div.nodeName + ',' + div.tagName + ',' + div.namespaceURI + ',' + "
            +   "div.prefix + ',' + div.localName);\n"
            + "      }\n"
            + "    </script>\n"
            + "  </head>\n"
            + "  <body onload='doTest()'>\n"
            + "  </body>\n"
            + "</html>";

        loadPageWithAlerts(html);
    }

    /**
     * Regression test for <tt>createTextNode</tt>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "Some Text", "9", "3", "Some Text", "#text" })
    public void createTextNode() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    var text1=document.createTextNode('Some Text');\n"
            + "    var body1=document.getElementById('body');\n"
            + "    body1.appendChild(text1);\n"
            + "    alert(text1.data);\n"
            + "    alert(text1.length);\n"
            + "    alert(text1.nodeType);\n"
            + "    alert(text1.nodeValue);\n"
            + "    alert(text1.nodeName);\n"
            + "}\n"
            + "</script></head><body onload='doTest()' id='body'>\n"
            + "</body></html>";

        final HtmlPage page = loadPageWithAlerts(html);

        final DomNode div1 = page.<HtmlElement>getHtmlElementById("body").getLastChild();
        assertEquals((short) 3, div1.getNodeType());
        assertEquals("Some Text", div1.getNodeValue());
        assertEquals("#text", div1.getNodeName());
    }

    /**
     * Verifies that when we create a text node and append it to an existing DOM node,
     * its <tt>outerHTML</tt>, <tt>innerHTML</tt> and <tt>innerText</tt> properties are
     * properly escaped.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(FF = { "<p>a & b</p> &amp; \u0162 \" '",
                "<p>a & b</p> &amp; \u0162 \" '",
                "undefined",
                "&lt;p&gt;a &amp; b&lt;/p&gt; &amp;amp; \u0162 \" '",
                "undefined" },
            IE = { "<p>a & b</p> &amp; \u0162 \" '",
                "<p>a & b</p> &amp; \u0162 \" '",
                "<DIV id=div>&lt;p&gt;a &amp; b&lt;/p&gt; &amp;amp; \u0162 \" '</DIV>",
                "&lt;p&gt;a &amp; b&lt;/p&gt; &amp;amp; \u0162 \" '",
                "<p>a & b</p> &amp; \u0162 \" '" })
    public void createTextNodeWithHtml_FF() throws Exception {
        final String html = "<html><body onload='test()'><script>\n"
            + "   function test() {\n"
            + "      var node = document.createTextNode('<p>a & b</p> &amp; \\u0162 \" \\'');\n"
            + "      alert(node.data);\n"
            + "      alert(node.nodeValue);\n"
            + "      var div = document.getElementById('div');\n"
            + "      div.appendChild(node);\n"
            + "      alert(div.outerHTML);\n"
            + "      alert(div.innerHTML);\n"
            + "      alert(div.innerText);\n"
            + "   };\n"
            + "</script>\n"
            + "<div id='div'></div>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Regression test for RFE 741930.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("1")
    public void appendChild() throws Exception {
        final String html
            = "<html><head><title>foo</title><script>\n"
            + "function doTest(){\n"
            + "    var form = document.forms['form1'];\n"
            + "    var div = document.createElement('DIV');\n"
            + "    form.appendChild(div);\n"
            + "    var elements = document.getElementsByTagName('DIV');\n"
            + "    alert(elements.length )\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<p>hello world</p>\n"
            + "<form name='form1'>\n"
            + "</form>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Verifies that <tt>document.appendChild()</tt> works in IE and doesn't work in FF.
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(IE = { "1", "2", "HTML", "DIV", "1" }, FF = { "1", "exception" })
    public void appendChildAtDocumentLevel() throws Exception {
        final String html =
              "<html>\n"
            + "<head>\n"
            + "  <title>test</title>\n"
            + "  <script>\n"
            + "    function test() {\n"
            + "      var div = document.createElement('div');\n"
            + "      div.innerHTML = 'test';\n"
            + "      try {\n"
            + "        alert(document.childNodes.length); // 1\n"
            + "        document.appendChild(div); // Error in FF\n"
            + "        alert(document.childNodes.length); // 2\n"
            + "        alert(document.childNodes[0].tagName); // HTML\n"
            + "        alert(document.childNodes[1].tagName); // DIV\n"
            + "        alert(document.getElementsByTagName('div').length); // 1\n"
            + "      } catch(ex) {\n"
            + "        alert('exception');\n"
            + "      }\n"
            + "    }\n"
            + "  </script>\n"
            + "</head>\n"
            + "<body onload='test()'></body>\n"
            + "</html>";

        loadPageWithAlerts(html);
    }

    /**
     * Regression test for appendChild of a text node.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("Some Text")
    public void appendChild_textNode() throws Exception {
        final String html
            = "<html><head><title>foo</title><script>\n"
            + "function doTest(){\n"
            + "    var form = document.forms['form1'];\n"
            + "    var child = document.createTextNode('Some Text');\n"
            + "    form.appendChild(child);\n"
            + "    alert(form.lastChild.data )\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<p>hello world</p>\n"
            + "<form name='form1'>\n"
            + "</form>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Regression test for <tt>cloneNode</tt>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "true", "true", "true", "true" })
    public void cloneNode() throws Exception {
        final String html
            = "<html><head><title>foo</title><script>\n"
            + "function doTest(){\n"
            + "    var form = document.forms['form1'];\n"
            + "    var cloneShallow = form.cloneNode(false);\n"
            + "    alert(cloneShallow!=null )\n"
            + "    alert(cloneShallow.firstChild==null )\n"
            + "    var cloneDeep = form.cloneNode(true);\n"
            + "    alert(cloneDeep!=null )\n"
            + "    alert(cloneDeep.firstChild!=null )\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<form name='form1'>\n"
            + "<p>hello world</p>\n"
            + "</form>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Regression test for <tt>insertBefore</tt>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("true")
    public void insertBefore() throws Exception {
        final String html
            = "<html><head><title>foo</title><script>\n"
            + "function doTest(){\n"
            + "    var form = document.forms['form1'];\n"
            + "    var oldChild = document.getElementById('oldChild');\n"
            + "    var div = document.createElement('DIV');\n"
            + "    form.insertBefore(div, oldChild);\n"
            + "    alert(form.firstChild==div )\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<form name='form1'><div id='oldChild'/></form>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void getBoxObjectFor() throws Exception {
        testHTMLFile("DocumentTest_getBoxObjectFor.html");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "bar", "null", "null" })
    public void getElementById() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    alert(top.document.getElementById('input1').value);\n"
            + "    alert(document.getElementById(''));\n"
            + "    alert(document.getElementById('non existing'));\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<form id='form1'>\n"
            + "<input id='input1' name='foo' type='text' value='bar' />\n"
            + "</form>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "bar", "null" })
    public void getElementById_resetId() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    var input1=top.document.getElementById('input1');\n"
            + "    input1.id='newId';\n"
            + "    alert(top.document.getElementById('newId').value);\n"
            + "    alert(top.document.getElementById('input1'));\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<form id='form1'>\n"
            + "<input id='input1' name='foo' type='text' value='bar' />\n"
            + "</form>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "bar" })
    public void getElementById_setNewId() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    var div1 = document.getElementById('div1');\n"
            + "    div1.firstChild.id='newId';\n"
            + "    alert(document.getElementById('newId').value);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<form id='form1'>\n"
            + "<div id='div1'><input name='foo' type='text' value='bar'></div>\n"
            + "</form>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Regression test for bug 740665.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "id1" })
    public void getElementById_divId() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    var element = document.getElementById('id1');\n"
            + "    alert(element.id);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<div id='id1'></div></body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Regression test for bug 740665.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "script1" })
    public void getElementById_scriptId() throws Exception {
        final String html
            = "<html><head><title>First</title><script id='script1'>\n"
            + "function doTest() {\n"
            + "    alert(top.document.getElementById('script1').id);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Regression test for bug 740665.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "text/javascript" })
    public void getElementById_scriptType() throws Exception {
        final String html
            = "<html><head><title>First</title>\n"
            + "<script id='script1' type='text/javascript'>\n"
            + "doTest=function () {\n"
            + "    alert(top.document.getElementById('script1').type);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Regression test for bug 740665.
     * @throws Exception if the test fails
     */
    @Test
    public void getElementById_scriptSrc() throws Exception {
        final WebClient webClient = getWebClient();
        final MockWebConnection webConnection = new MockWebConnection();
        webClient.setWebConnection(webConnection);

        final String html
            = "<html><head><title>First</title>\n"
            + "<script id='script1' src='http://script'>\n"
            + "</script></head><body onload='doTest()'>\n"
            + "</body></html>";
        webConnection.setResponse(URL_FIRST, html);

        final String script
            = "doTest=function () {\n"
            + "    alert(top.document.getElementById('script1').src);\n"
            + "}";
        webConnection.setResponse(new URL("http://script/"), script, "text/javascript");

        final List<String> collectedAlerts = new ArrayList<String>();
        webClient.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

        final HtmlPage firstPage = webClient.getPage(URL_FIRST);
        assertEquals("First", firstPage.getTitleText());

        final String[] expectedAlerts = {"http://script"};
        assertEquals(expectedAlerts, collectedAlerts);
    }

    /**
     * Regression test for <tt>parentNode</tt> with nested elements.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "parentDiv" })
    public void parentNode_Nested() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    var div1=document.getElementById('childDiv');\n"
            + "    alert(div1.parentNode.id);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<div id='parentDiv'><div id='childDiv'></div></div>\n"
            + "</body></html>";

        final HtmlPage firstPage = loadPageWithAlerts(html);

        final HtmlElement div1 = firstPage.getHtmlElementById("childDiv");
        assertEquals("parentDiv", ((HtmlElement) div1.getParentNode()).getAttribute("id"));
    }

    /**
     * Regression test for <tt>parentNode</tt> of document.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "true" })
    public void parentNode_Document() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    alert(document.parentNode==null);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Regression test for <tt>parentNode</tt> and <tt>createElement</tt>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "true" })
    public void parentNode_CreateElement() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    var div1=document.createElement('div');\n"
            + "    alert(div1.parentNode==null);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Regression test for <tt>parentNode</tt> and <tt>appendChild</tt>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "parentDiv" })
    public void parentNode_AppendChild() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    var childDiv=document.getElementById('childDiv');\n"
            + "    var parentDiv=document.getElementById('parentDiv');\n"
            + "    parentDiv.appendChild(childDiv);\n"
            + "    alert(childDiv.parentNode.id);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<div id='parentDiv'></div><div id='childDiv'></div>\n"
            + "</body></html>";

        final HtmlPage firstPage = loadPageWithAlerts(html);

        final HtmlElement childDiv = firstPage.getHtmlElementById("childDiv");
        assertEquals("parentDiv", ((HtmlElement) childDiv.getParentNode()).getAttribute("id"));
    }

    /**
     * Regression test for <tt>documentElement</tt> of document.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "true", "HTML", "true" })
    public void documentElement() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    alert(document.documentElement!=null);\n"
            + "    alert(document.documentElement.tagName);\n"
            + "    alert(document.documentElement.parentNode==document);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Regression test for <tt>firstChild</tt> with nested elements.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "childDiv" })
    public void firstChild_Nested() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    var div1=document.getElementById('parentDiv');\n"
            + "    alert(div1.firstChild.id);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<div id='parentDiv'><div id='childDiv'/><div id='childDiv2'/></div>\n"
            + "</body></html>";

        final HtmlPage firstPage = loadPageWithAlerts(html);
        assertEquals("First", firstPage.getTitleText());

        final HtmlElement div1 = firstPage.getHtmlElementById("parentDiv");
        assertEquals("childDiv", ((HtmlElement) div1.getFirstChild()).getAttribute("id"));
    }

    /**
     * Regression test for <tt>firstChild</tt> and <tt>appendChild</tt>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "childDiv" })
    public void firstChild_AppendChild() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    var childDiv=document.getElementById('childDiv');\n"
            + "    var parentDiv=document.getElementById('parentDiv');\n"
            + "    parentDiv.appendChild(childDiv);\n"
            + "    var childDiv2=document.getElementById('childDiv2');\n"
            + "    parentDiv.appendChild(childDiv2);\n"
            + "    alert(parentDiv.firstChild.id);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<div id='parentDiv'/><div id='childDiv'/><div id='childDiv2'/>\n"
            + "</body></html>";

        final HtmlPage firstPage = loadPageWithAlerts(html);
        assertEquals("First", firstPage.getTitleText());

        final HtmlElement parentDiv = firstPage.getHtmlElementById("parentDiv");
        assertEquals("childDiv", ((HtmlElement) parentDiv.getFirstChild()).getAttribute("id"));
    }

    /**
     * Regression test for lastChild with nested elements.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "childDiv" })
    public void lastChild_Nested() throws Exception {
        final String html
            = "<html><head><title>Last</title><script>\n"
            + "function doTest() {\n"
            + "    var div1=document.getElementById('parentDiv');\n"
            + "    alert(div1.lastChild.id);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<div id='parentDiv'><div id='childDiv1'></div><div id='childDiv'></div></div>\n"
            + "</body></html>";

        final HtmlPage lastPage = loadPageWithAlerts(html);
        assertEquals("Last", lastPage.getTitleText());

        final HtmlElement parentDiv = lastPage.getHtmlElementById("parentDiv");
        assertEquals("childDiv", ((HtmlElement) parentDiv.getLastChild()).getAttribute("id"));
    }

    /**
     * Regression test for lastChild and appendChild.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "childDiv" })
    public void lastChild_AppendChild() throws Exception {
        final String html
            = "<html><head><title>Last</title><script>\n"
            + "function doTest() {\n"
            + "    var childDiv1=document.getElementById('childDiv1');\n"
            + "    var parentDiv=document.getElementById('parentDiv');\n"
            + "    parentDiv.appendChild(childDiv1);\n"
            + "    var childDiv=document.getElementById('childDiv');\n"
            + "    parentDiv.appendChild(childDiv);\n"
            + "    alert(parentDiv.lastChild.id);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<div id='parentDiv'/><div id='childDiv1'/><div id='childDiv'/>\n"
            + "</body></html>";

        final HtmlPage lastPage = loadPageWithAlerts(html);
        assertEquals("Last", lastPage.getTitleText());

        final HtmlElement parentDiv = lastPage.getHtmlElementById("parentDiv");
        assertEquals("childDiv", ((HtmlElement) parentDiv.getLastChild()).getAttribute("id"));
    }

    /**
     * Regression test for nextSibling with nested elements.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "nextDiv" })
    public void nextSibling_Nested() throws Exception {
        final String html
            = "<html><head><title>Last</title><script>\n"
            + "function doTest() {\n"
            + "    var div1 = document.getElementById('previousDiv');\n"
            + "    alert(div1.nextSibling.id);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<div id='parentDiv'><div id='previousDiv'></div><div id='nextDiv'></div></div>\n"
            + "</body></html>";

        final HtmlPage lastPage = loadPageWithAlerts(html);
        assertEquals("Last", lastPage.getTitleText());

        final HtmlElement div1 = lastPage.getHtmlElementById("previousDiv");
        assertEquals("nextDiv", ((HtmlElement) div1.getNextSibling()).getAttribute("id"));
    }

    /**
     * Regression test for nextSibling and appendChild.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "nextDiv" })
    public void nextSibling_AppendChild() throws Exception {
        final String html
            = "<html><head><title>Last</title><script>\n"
            + "function doTest() {\n"
            + "    var previousDiv=document.getElementById('previousDiv');\n"
            + "    var parentDiv=document.getElementById('parentDiv');\n"
            + "    parentDiv.appendChild(previousDiv);\n"
            + "    var nextDiv=document.getElementById('nextDiv');\n"
            + "    parentDiv.appendChild(nextDiv);\n"
            + "    alert(previousDiv.nextSibling.id);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<div id='parentDiv'/><div id='junk1'/><div id='previousDiv'/><div id='junk2'/><div id='nextDiv'/>\n"
            + "</body></html>";

        final HtmlPage lastPage = loadPageWithAlerts(html);
        assertEquals("Last", lastPage.getTitleText());

        final HtmlElement previousDiv = lastPage.getHtmlElementById("previousDiv");
        assertEquals("nextDiv", ((HtmlElement) previousDiv.getNextSibling()).getAttribute("id"));
    }

    /**
     * Regression test for previousSibling with nested elements.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "previousDiv" })
    public void previousSibling_Nested() throws Exception {
        final String html
            = "<html><head><title>Last</title><script>\n"
            + "function doTest() {\n"
            + "    var div1 = document.getElementById('nextDiv');\n"
            + "    alert(div1.previousSibling.id);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<div id='parentDiv'><div id='previousDiv'></div><div id='nextDiv'></div></div>\n"
            + "</body></html>";

        final HtmlPage lastPage = loadPageWithAlerts(html);
        assertEquals("Last", lastPage.getTitleText());

        final HtmlElement div1 = lastPage.getHtmlElementById("nextDiv");
        assertEquals("previousDiv", ((HtmlElement) div1.getPreviousSibling()).getAttribute("id"));
    }

    /**
     * Regression test for previousSibling and appendChild.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "previousDiv" })
    public void previousSibling_AppendChild() throws Exception {
        final String html
            = "<html><head><title>Last</title><script>\n"
            + "function doTest() {\n"
            + "    var previousDiv=document.getElementById('previousDiv');\n"
            + "    var parentDiv=document.getElementById('parentDiv');\n"
            + "    parentDiv.appendChild(previousDiv);\n"
            + "    var nextDiv=document.getElementById('nextDiv');\n"
            + "    parentDiv.appendChild(nextDiv);\n"
            + "    alert(nextDiv.previousSibling.id);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<div id='parentDiv'/><div id='junk1'/><div id='previousDiv'/><div id='junk2'/><div id='nextDiv'/>\n"
            + "</body></html>";

        final HtmlPage lastPage = loadPageWithAlerts(html);
        assertEquals("Last", lastPage.getTitleText());

        final HtmlElement nextDiv = lastPage.getHtmlElementById("nextDiv");
        assertEquals("previousDiv", ((HtmlElement) nextDiv.getPreviousSibling()).getAttribute("id"));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "tangerine", "ginger" })
    public void allProperty_KeyByName() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    alert(document.all['input1'].value);\n"
            + "    alert(document.all['foo2'].value);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'><form id='form1'>\n"
            + "    <input id='input1' name='foo1' type='text' value='tangerine' />\n"
            + "    <input id='input2' name='foo2' type='text' value='ginger' />\n"
            + "</form>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Regression test for bug 707750.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "DIV" })
    public void allProperty_CalledDuringPageLoad() throws Exception {
        final String html
            = "<html><body>\n"
            + "<div id='ARSMenuDiv1' style='VISIBILITY: hidden; POSITION: absolute; z-index: 1000000'></div>\n"
            + "<script language='Javascript'>\n"
            + "    var divObj = document.all['ARSMenuDiv1'];\n"
            + "    alert(divObj.tagName);\n"
            + "</script></body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void write() throws Exception {
        final String html
            = "<html><head><title>First</title></head><body>\n"
            + "<script>\n"
            + "document.write(\"<div id='div1'></div>\");\n"
            + "document.write('<div', \" id='div2'>\", '</div>');\n"
            + "document.writeln('<div', \" id='div3'>\", '</div>');\n"
            + "</script>\n"
            + "</form></body></html>";

        final HtmlPage page = loadPage(html);
        assertEquals("First", page.getTitleText());

        page.getHtmlElementById("div1");
        page.getHtmlElementById("div2");
        page.getHtmlElementById("div3");
    }

    /**
     * Regression test for bug 743241.
     * @throws Exception if the test fails
     */
    @Test
    public void write_LoadScript() throws Exception {
        final WebClient webClient = getWebClient();
        final MockWebConnection webConnection = new MockWebConnection();
        webClient.setWebConnection(webConnection);

        final String html
            = "<html><head><title>First</title></head><body>\n"
            + "<script src='http://script'></script>\n"
            + "</form></body></html>";
        webConnection.setResponse(URL_FIRST, html);

        final String script = "document.write(\"<div id='div1'></div>\");\n";
        webConnection.setResponse(new URL("http://script/"), script, "text/javascript");

        final List<String> collectedAlerts = new ArrayList<String>();
        webClient.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

        final HtmlPage page = webClient.getPage(URL_FIRST);
        assertEquals("First", page.getTitleText());

        try {
            page.getHtmlElementById("div1");
        }
        catch (final ElementNotFoundException e) {
            fail("Element not written to page as expected");
        }
    }

    /**
     * Regression test for bug 715379.
     * @throws Exception if the test fails
     */
    @Test
    public void write_script() throws Exception {
        final WebClient webClient = getWebClient();
        final MockWebConnection webConnection = new MockWebConnection();
        webClient.setWebConnection(webConnection);

        final String mainHtml
            = "<html><head><title>Main</title></head><body>\n"
            + "<iframe name='iframe' id='iframe' src='http://first'></iframe>\n"
            + "<script type='text/javascript'>\n"
            + "document.write('<script type=\"text/javascript\" src=\"http://script\"></' + 'script>');\n"
            + "</script></body></html>";
        webConnection.setResponse(new URL("http://main/"), mainHtml);

        final String firstHtml = "<html><body><h1 id='first'>First</h1></body></html>";
        webConnection.setResponse(URL_FIRST, firstHtml);

        final String secondHtml = "<html><body><h1 id='second'>Second</h1></body></html>";
        webConnection.setResponse(URL_SECOND, secondHtml);

        final String script = "document.getElementById('iframe').src = '" + URL_SECOND + "';\n";
        webConnection.setResponse(new URL("http://script/"), script, "text/javascript");

        final List<String> collectedAlerts = new ArrayList<String>();
        webClient.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

        final HtmlPage mainPage = webClient.getPage("http://main");
        assertEquals("Main", mainPage.getTitleText());

        final HtmlInlineFrame iFrame = mainPage.getHtmlElementById("iframe");

        assertEquals(URL_SECOND.toExternalForm(), iFrame.getSrcAttribute());

        final HtmlPage enclosedPage = (HtmlPage) iFrame.getEnclosedPage();
        // This will blow up if the script hasn't been written to the document
        // and executed so the second page has been loaded.
        enclosedPage.getHtmlElementById("second");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "A" })
    public void write_InDOM() throws Exception {
        final String html
            = "<html><head><title>First</title></head><body>\n"
            + "<script type='text/javascript'>\n"
            + "document.write('<a id=\"blah\">Hello World</a>');\n"
            + "document.write('<a id=\"blah2\">Hello World 2</a>');\n"
            + "alert(document.getElementById('blah').tagName);\n"
            + "</script>\n"
            + "<a id='blah3'>Hello World 3</a>\n"
            + "</body></html>";

        final HtmlPage page = loadPageWithAlerts(html);

        assertEquals("First", page.getTitleText());
        assertEquals(3, page.getElementsByTagName("a").getLength());
    }

    /**
     * Verifies that document.write() sends content to the correct destination (always somewhere in the body).
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(IE = { "null", "[object]", "s1 s2 s3 s4 s5" },
            FF = { "null", "[object HTMLBodyElement]", "s1 s2 s3 s4 s5" })
    public void write_Destination() throws Exception {
        final String html =
              "<html>\n"
            + "  <head>\n"
            + "    <script>alert(document.body);</script>\n"
            + "    <script>document.write('<span id=\"s1\">1</span>');</script>\n"
            + "    <script>alert(document.body);</script>\n"
            + "    <title>test</title>\n"
            + "    <script>document.write('<span id=\"s2\">2</span>');</script>\n"
            + "  </head>\n"
            + "  <body id='foo'>\n"
            + "    <script>document.write('<span id=\"s3\">3</span>');</script>\n"
            + "    <span id='s4'>4</span>\n"
            + "    <script>document.write('<span id=\"s5\">5</span>');</script>\n"
            + "    <script>\n"
            + "      var s = '';\n"
            + "      for(var n = document.body.firstChild; n; n = n.nextSibling) {\n"
            + "        if(n.id) {\n"
            + "          if(s.length > 0) s+= ' ';\n"
            + "            s += n.id;\n"
            + "        }\n"
            + "      }\n"
            + "      alert(s);\n"
            + "    </script>\n"
            + "  </body>\n"
            + "</html>";

        loadPageWithAlerts(html);
    }

    /**
     * Verifies that document.write() sends content to the correct destination (always somewhere in the body),
     * and that if a synthetic temporary body needs to be created, the attributes of the real body are eventually
     * used once the body is parsed.
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(IE = { "null", "[object]", "", "foo" },
            FF = { "null", "[object HTMLBodyElement]", "", "foo" })
    public void write_BodyAttributesKept() throws Exception {
        final String html =
              "<html>\n"
            + "  <head>\n"
            + "    <script>alert(document.body);</script>\n"
            + "    <script>document.write('<span id=\"s1\">1</span>');</script>\n"
            + "    <script>alert(document.body);</script>\n"
            + "    <script>alert(document.body.id);</script>\n"
            + "    <title>test</title>\n"
            + "  </head>\n"
            + "  <body id='foo'>\n"
            + "    <script>alert(document.body.id);</script>\n"
            + "  </body>\n"
            + "</html>";

        loadPageWithAlerts(html);
    }

    /**
     * Verifies that document.write() sends content to the correct destination (always somewhere in the body),
     * and that script elements written to the document are executed in the correct order.
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts({ "1", "2", "3" })
    public void write_ScriptExecutionOrder() throws Exception {
        final String html =
              "<html>\n"
            + "  <head>\n"
            + "    <title>test</title>\n"
            + "    <script>alert('1');</script>\n"
            + "    <script>document.write('<scrip'+'t>alert(\"2\")</s'+'cript>');</script>\n"
            + "  </head>\n"
            + "  <body>\n"
            + "    <script>document.write('<scrip'+'t>alert(\"3\")</s'+'cript>');</script>\n"
            + "  </body>\n"
            + "</html>";

        loadPageWithAlerts(html);
    }

    /**
     * IE accepts the use of detached functions, but FF doesn't.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(IE = { "" }, FF = { "exception occurred" })
    public void write_AssignedToVar() throws Exception {
        final String html
            = "<html><head><title>foo</title><script>\n"
            + "function doTheFoo() {\n"
            + "var d = document.writeln\n"
            + "try {\n"
            + "  d('foo')\n"
            + "} catch (e) { alert('exception occurred') }\n"
            + "  document.writeln('foo')\n"
            + "}\n"
            + "</script></head><body onload='doTheFoo()'>\n"
            + "<p>hello world</p>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Test for bug 1950462: calling document.write inside a function (after assigning
     * document.write to a local variable) tries to invoke document.write on the prototype
     * document instance, rather than the actual document host object. This leads to an
     * {@link IllegalStateException} (DomNode has not been set for this SimpleScriptable).
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(IE = { "" }, FF = { "exception occurred" })
    public void write_AssignedToVar2() throws Exception {
        final String html =
            "<html><head><title>Test</title></head><body>\n"
            + "<script>\n"
            + "  function foo() { var d = document.write; d(4); }\n"
            + "  try {"
            + "    foo();"
            + "  } catch (e) { alert('exception occurred'); document.write(4); }\n"
            + "</script>\n"
            + "</body></html>";
        final HtmlPage page = loadPage(getBrowserVersion(), html, null);
        assertEquals("Test", page.getTitleText());
        assertEquals("4", page.getBody().asText());
    }

    /**
     * Verifies that calling document.write() after document parsing has finished results in an whole
     * new page being loaded.
     * @throws Exception if an error occurs
     */
    @Test
    public void write_WhenParsingFinished() throws Exception {
        final String html =
              "<html><head><script>\n"
            + "  function test() { document.write(1); document.write(2); document.close(); }\n"
            + "</script></head>\n"
            + "<body><span id='s' onclick='test()'>click</span></body></html>";

        HtmlPage page = loadPage(getBrowserVersion(), html, null);
        assertEquals("click", page.getBody().asText());

        final HtmlSpan span = page.getHtmlElementById("s");
        page = span.click();
        assertEquals("12", page.getBody().asText());
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    public void locationAfterWrite() throws Exception {
        final String html =
              "<html><head><script>\n"
            + "function test() { \n"
            + "  alert(document.location);\n"
            + "  document.open();\n"
            + "  document.write('<html><body onload=\"alert(document.location)\"></body></html>');\n"
            + "  document.close();\n"
            + "}"
            + "</script></head>\n"
            + "<body onload='test()'></body></html>";

        final String[] expectedAlerts = {URL_GARGOYLE.toExternalForm(), URL_GARGOYLE.toExternalForm()};

        final List<String> collectedAlerts = new ArrayList<String>();
        loadPage(getBrowserVersion(), html, collectedAlerts);
        assertEquals(expectedAlerts, collectedAlerts);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts({ "", "First", "First", "FORM", "true", "true" })
    public void newElementsAfterWrite() throws Exception {
        final String html =
              "<html><head><script>\n"
            + "function test() { \n"
            + "  alert(document.title);\n"
            + "  document.open();\n"
            + "  document.write('<html><head><title>First</title></head>');"
            + "  document.write('<body onload=\"alert(document.title)\">');"
            + "  document.write('<form name=\"submitForm\" method=\"post\">');"
            + "  document.write('</form></body></html>');\n"
            + "  document.close();\n"
            + "  alert(document.title);\n"
            + "  alert(document.submitForm.tagName);\n"
            + "  alert(window.document == document);\n"
            + "  alert(document.submitForm == document.getElementsByTagName('form')[0]);\n"
            + "}"
            + "</script></head>\n"
            + "<body onload='test()'></body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Verifies that calls to document.open() are ignored while the page's HTML is being parsed.
     * @throws Exception if an error occurs
     */
    @Test
    public void open_IgnoredDuringParsing() throws Exception {
        final String html = "<html><body>1<script>document.open();document.write('2');</script>3</body></html>";
        final HtmlPage page = loadPage(getBrowserVersion(), html, null);
        assertEquals("123", page.getBody().asText());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void referrer() throws Exception {
        final WebClient webClient = getWebClient();
        final MockWebConnection webConnection = new MockWebConnection();

        final String html
            = "<html><head><title>First</title></head><body onload='alert(document.referrer);'>\n"
            + "</form></body></html>";

        final List<NameValuePair> responseHeaders =
            Collections.singletonList(new NameValuePair("referrer", "http://ref"));
        webConnection.setResponse(URL_FIRST, html, 200, "OK", "text/html", responseHeaders);
        webClient.setWebConnection(webConnection);

        final List<String> collectedAlerts = new ArrayList<String>();
        webClient.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

        final HtmlPage firstPage = webClient.getPage(URL_FIRST);
        assertEquals("First", firstPage.getTitleText());

        assertEquals(new String[] {"http://ref"}, collectedAlerts);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void referrer_NoneSpecified() throws Exception {
        final String html
            = "<html><head><title>First</title></head><body onload='alert(document.referrer);'>\n"
            + "</form></body></html>";

        final List<String> collectedAlerts = new ArrayList<String>();
        final HtmlPage firstPage = loadPage(getBrowserVersion(), html, collectedAlerts);
        assertEquals("First", firstPage.getTitleText());

        assertEquals(new String[] {""}, collectedAlerts);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void url() throws Exception {
        final String html
            = "<html><head><title>First</title></head><body onload='alert(document.URL);'>\n"
            + "</form></body></html>";

        final List<String> collectedAlerts = new ArrayList<String>();
        final HtmlPage firstPage = loadPage(getBrowserVersion(), html, collectedAlerts);
        assertEquals("First", firstPage.getTitleText());

        assertEquals(new String[] {URL_GARGOYLE.toExternalForm()}, collectedAlerts);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "button", "button", "true" })
    public void getElementsByTagName() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    var elements = document.getElementsByTagName('input');\n"
            + "    for (var i=0; i<elements.length; i++) {\n"
            + "        alert(elements[i].type);\n"
            + "        alert(elements.item(i).type);\n"
            + "    }\n"
            + "    alert(elements == document.getElementsByTagName('input'));\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<form><input type='button' name='button1' value='pushme'></form>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Regression test for bug 740636.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "button" })
    public void getElementsByTagName_CaseInsensitive() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    var elements = document.getElementsByTagName('InPuT');\n"
            + "    for(i=0; i<elements.length; i++ ) {\n"
            + "        alert(elements[i].type);\n"
            + "    }\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<form><input type='button' name='button1' value='pushme'></form>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Regression test for bug 740605.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "1" })
    public void getElementsByTagName_Inline() throws Exception {
        final String html
            = "<html><body><script type=\"text/javascript\">\n"
            + "alert(document.getElementsByTagName('script').length);\n"
            + "</script></body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Regression test for bug 740605.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "1" })
    public void getElementsByTagName_LoadScript() throws Exception {
        final WebClient webClient = getWebClient();
        final MockWebConnection webConnection = new MockWebConnection();
        webClient.setWebConnection(webConnection);

        final String html = "<html><body><script src=\"http://script\"></script></body></html>";
        webConnection.setResponse(URL_FIRST, html);

        final String script = "alert(document.getElementsByTagName('script').length);\n";
        webConnection.setResponse(new URL("http://script/"), script, "text/javascript");

        final List<String> collectedAlerts = new ArrayList<String>();
        webClient.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

        webClient.getPage(URL_FIRST);

        assertEquals(getExpectedAlerts(), collectedAlerts);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "HTML", "HEAD", "TITLE", "SCRIPT", "BODY" })
    public void all_WithParentheses() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    var length = document.all.length;\n"
            + "    for(i=0; i< length; i++ ) {\n"
            + "        alert(document.all(i).tagName);\n"
            + "    }\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "HTML", "HEAD", "TITLE", "SCRIPT", "BODY" })
    public void all_IndexByInt() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    var length = document.all.length;\n"
            + "    for(i=0; i< length; i++ ) {\n"
            + "        alert(document.all[i].tagName);\n"
            + "    }\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "HTML" })
    public void all_Item() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    alert(document.all.item(0).tagName);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "form1", "form2", "2" })
    public void all_NamedItem() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    alert(document.all.namedItem('form1').name);\n"
            + "    alert(document.all.namedItem('form2').id);\n"
            + "    alert(document.all.namedItem('form3').length);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<form name='form1'></form>\n"
            + "<form id='form2'></form>\n"
            + "<form name='form3'></form>\n"
            + "<form name='form3'></form>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "a", "b", "a", "b", "0" })
    public void all_tags() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    var inputs = document.all.tags('input');\n"
            + "    var inputCount = inputs.length;\n"
            + "    for(i=0; i< inputCount; i++ ) {\n"
            + "        alert(inputs[i].name);\n"
            + "    }\n"
            + "    // Make sure tags() returns an element array that you can call item() on.\n"
            + "    alert(document.all.tags('input').item(0).name);\n"
            + "    alert(document.all.tags('input').item(1).name);\n"
            + "    // Make sure tags() returns an empty element array if there are no matches.\n"
            + "    alert(document.all.tags('xxx').length);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<input type='text' name='a' value='1'>\n"
            + "<input type='text' name='b' value='1'>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Firefox supports document.all, but it is "hidden".
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(FF = { "false", "true" },
            IE = { "true", "true" })
    public void all_AsBoolean() throws Exception {
        final String html = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    alert(document.all ? true : false);\n"
            + "    alert(Boolean(document.all));\n"
            + "}\n"
            + "</script><body onload='doTest()'>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Makes sure that the document.all collection contents are not cached if the
     * collection is accessed before the page has finished loading.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "1", "2" })
    public void all_Caching() throws Exception {
        final String html
            = "<html><head><title>Test</title></head>\n"
            + "<body onload='alert(document.all.b.value)'>\n"
            + "<input type='text' name='a' value='1'>\n"
            + "<script>alert(document.all.a.value)</script>\n"
            + "<input type='text' name='b' value='2'>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "null" })
    public void all_NotExisting() throws Exception {
        final String html = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    alert(document.all('notExisting'));\n"
            + "}\n"
            + "</script><body onload='doTest()'>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void cookie_read() throws Exception {
        final WebClient webClient = getWebClient();
        final MockWebConnection webConnection = new MockWebConnection();

        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    var cookieSet = document.cookie.split('; ');\n"
            + "    var setSize = cookieSet.length;\n"
            + "    var crumbs;\n"
            + "    var x=0;\n"
            + "    for (x=0;((x<setSize)); x++) {\n"
            + "        crumbs = cookieSet[x].split('=');\n"
            + "        alert (crumbs[0]);\n"
            + "        alert (crumbs[1]);\n"
            + "    } \n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "</body></html>";

        final URL url = URL_FIRST;
        webConnection.setResponse(url, html);
        webClient.setWebConnection(webConnection);

        final CookieManager mgr = webClient.getCookieManager();
        mgr.addCookie(new Cookie("first", "one", "two", "/", -1, false));
        mgr.addCookie(new Cookie("first", "three", "four", "/", -1, false));

        final List<String> collectedAlerts = new ArrayList<String>();
        webClient.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

        final HtmlPage firstPage = webClient.getPage(URL_FIRST);
        assertEquals("First", firstPage.getTitleText());

        final String[] expectedAlerts = {"one", "two", "three", "four" };
        assertEquals(expectedAlerts, collectedAlerts);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    public void cookie_write() throws Exception {
        cookie_write(true);
        cookie_write(false);
    }

    private void cookie_write(final boolean cookiesEnabled) throws Exception {
        final String html =
              "<html>\n"
            + "    <head>\n"
            + "        <script>\n"
            + "            alert(navigator.cookieEnabled);\n"
            + "            alert(document.cookie);\n"
            + "            document.cookie = 'foo=bar';\n"
            + "            alert(document.cookie);\n"
            + "        </script>\n"
            + "    </head>\n"
            + "    <body>abc</body>\n"
            + "</html>";

        final WebClient client = getWebClient();
        client.getCookieManager().setCookiesEnabled(cookiesEnabled);

        final List<String> actual = new ArrayList<String>();
        client.setAlertHandler(new CollectingAlertHandler(actual));

        final MockWebConnection conn = new MockWebConnection();
        conn.setDefaultResponse(html);
        client.setWebConnection(conn);

        client.getPage("http://www.microsoft.com/");

        final String[] expected;
        if (cookiesEnabled) {
            expected = new String[] {"true", "", "foo=bar"};
        }
        else {
            expected = new String[] {"false", "", ""};
        }

        assertEquals(expected, actual);
    }

    /**
     * Verifies that cookies work when working with local files (not remote sites with real domains).
     * Required for local testing of Dojo 1.1.1.
     * @throws Exception if an error occurs
     */
    @Test
    public void cookieInLocalFile() throws Exception {
        final WebClient client = getWebClient();

        final List<String> actual = new ArrayList<String>();
        client.setAlertHandler(new CollectingAlertHandler(actual));

        final URL url = getClass().getResource("DocumentTest_cookieInLocalFile.html");
        client.getPage(url);

        final String[] expected = new String[] {"", "", "blah=bleh"};
        assertEquals(expected, actual);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "value1", "value1", "value2", "value2" })
    public void getElementsByName() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    var elements = document.getElementsByName('name1');\n"
            + "    for (var i=0; i<elements.length; i++) {\n"
            + "        alert(elements[i].value);\n"
            + "        alert(elements.item(i).value);\n"
            + "    }\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<form>\n"
            + "<input type='radio' name='name1' value='value1'>\n"
            + "<input type='radio' name='name1' value='value2'>\n"
            + "<input type='button' name='name2' value='value3'>\n"
            + "</form>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "IAmTheBody" })
    public void body_read() throws Exception {
        final String html = "<html><head><title>First</title></head>\n"
            + "<body id='IAmTheBody' onload='alert(document.body.id)'>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "FRAMESET" })
    public void body_readFrameset() throws Exception {
        final String html = "<html>\n"
            + "<frameset onload='alert(document.body.tagName)'>\n"
            + "<frame src='about:blank' name='foo'>\n"
            + "</frameset></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Test the access to the images value. This should return the 2 images in the document
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "1", "2", "2", "true" })
    public void images() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function doTest() {\n"
            + "    alert(document.images.length);\n"
            + "    alert(allImages.length);\n"
            + "    alert(document.images == allImages);\n"
            + "}\n"
            + "</script></head><body onload='doTest()'>\n"
            + "<img src='firstImage'>\n"
            + "<script>\n"
            + "var allImages = document.images;\n"
            + "alert(allImages.length);\n"
            + "</script>\n"
            + "<form>\n"
            + "<img src='2ndImage'>\n"
            + "</form>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Test setting and reading the title for an existing title.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "correct title" })
    public void settingTitle() throws Exception {
        final String html
            = "<html><head><title>Bad Title</title></head>\n"
            + "<body>\n"
            + "<script>\n"
            + "    document.title = 'correct title';\n"
            + "    alert(document.title)\n"
            + "</script>\n"
            + "</body></html>";

        final HtmlPage page = loadPageWithAlerts(html);
        assertEquals("correct title", page.getTitleText());
    }

    /**
     * Test setting and reading the title for when the is not in the page to begin.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "correct title" })
    public void settingMissingTitle() throws Exception {
        final String html = "<html><head></head>\n"
            + "<body>\n"
            + "<script>\n"
            + "    document.title = 'correct title';\n"
            + "    alert(document.title)\n"
            + "</script>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Test setting and reading the title for when the is not in the page to begin.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "correct title" })
    public void settingBlankTitle() throws Exception {
        final String html = "<html><head><title></title></head>\n"
            + "<body>\n"
            + "<script>\n"
            + "    document.title = 'correct title';\n"
            + "    alert(document.title)\n"
            + "</script>\n"
            + "</body></html>";

        final HtmlPage page = loadPageWithAlerts(html);
        assertEquals("correct title", page.getTitleText());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "foo" })
    public void title() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "function doTest(){\n"
            + "    alert(document.title)\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='doTest()'>\n"
            + "</body></html>";

        final HtmlPage page = loadPageWithAlerts(html);
        assertEquals("foo", page.getTitleText());
    }

    /**
     * Test the ReadyState which is an IE feature.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(FF = { "undefined", "undefined" },
            IE = { "loading", "complete" })
    public void readyState() throws Exception {
        final String html = "<html><head>\n"
            + "<script>\n"
            + "function testIt() {\n"
            + "  alert(document.readyState);\n"
            + "}\n"
            + "alert(document.readyState);\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onLoad='testIt()'></body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Calling document.body before the page is fully loaded used to cause an exception.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("null")
    public void documentWithNoBody() throws Exception {
        final String html
            = "<html><head><title>foo</title><script>\n"
            + "alert(document.body)\n"
            + "</script></head><body></body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * IE has a bug which returns the element by name if it cannot find it by ID.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(FF = { "null", "byId" },
            IE = { "findMe", "byId" })
    public void getElementById_findByName() throws Exception {
        final String html
            = "<html><head><title>foo</title></head>\n"
            + "<body>\n"
            + "<input type='text' name='findMe'>\n"
            + "<input type='text' id='findMe2' name='byId'>\n"
            + "<script>\n"
            + "var o = document.getElementById('findMe')\n"
            + "alert(o ? o.name : 'null')\n"
            + "alert(document.getElementById('findMe2').name)\n"
            + "</script></body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Browsers(Browser.NONE)
    public void buildCookie() throws Exception {
        checkCookie(HTMLDocument.buildCookie("toto=foo", URL_FIRST), "toto", "foo", "", "first", false, null);
        checkCookie(HTMLDocument.buildCookie("toto=", URL_FIRST), "toto", "", "", "first", false, null);
        checkCookie(HTMLDocument.buildCookie("toto=foo;secure", URL_FIRST), "toto", "foo", "", "first", true, null);
        checkCookie(HTMLDocument.buildCookie("toto=foo;path=/myPath;secure", URL_FIRST),
                "toto", "foo", "/myPath", "first", true, null);

        // Check that leading and trailing whitespaces are ignored
        checkCookie(HTMLDocument.buildCookie("   toto=foo;  path=/myPath  ; secure  ", URL_FIRST),
                "toto", "foo", "/myPath", "first", true, null);

        // Check that we accept reserved attribute names (e.g expires, domain) in any case
        checkCookie(HTMLDocument.buildCookie("toto=foo; PATH=/myPath; SeCURE", URL_FIRST),
                "toto", "foo", "/myPath", "first", true, null);

        // Check that we are able to parse and set the expiration date correctly
        final String dateString = "Fri, 21 Jul 2006 20:47:11 UTC";
        final Date date = DateUtil.parseDate(dateString);
        checkCookie(HTMLDocument.buildCookie("toto=foo; expires=" + dateString, URL_FIRST),
                "toto", "foo", "", "first", false, date);
    }

    private void checkCookie(final Cookie cookie, final String name, final String value,
            final String path, final String domain, final boolean secure, final Date date) {
        assertEquals(name, cookie.getName());
        assertEquals(value, cookie.getValue());
        assertNull(cookie.getComment());
        assertEquals(path, cookie.getPath());
        assertEquals(domain, cookie.getDomain());
        assertEquals(secure, cookie.getSecure());
        assertEquals(date, cookie.getExpiryDate());
    }

    /**
     * Test that <tt>img</tt> and <tt>form</tt> can be retrieved directly by name, but not <tt>a</tt>, <tt>input</tt>
     * or <tt>button</tt>.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "myImageId", "2", "FORM", "undefined", "undefined", "undefined", "undefined" })
    public void directAccessByName() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "function doTest(){\n"
            + "    alert(document.myImage.id);\n"
            + "    alert(document.myImage2.length);\n"
            + "    alert(document.myForm.tagName);\n"
            + "    alert(document.myAnchor);\n"
            + "    alert(document.myInput);\n"
            + "    alert(document.myInputImage);\n"
            + "    alert(document.myButton);\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='doTest()'>\n"
            + "<img src='foo' name='myImage' id='myImageId'>\n"
            + "<img src='foo' name='myImage2'>\n"
            + "<img src='foo' name='myImage2'>\n"
            + "<a name='myAnchor'/>\n"
            + "<form name='myForm'>\n"
            + "<input name='myInput' type='text'>\n"
            + "<input name='myInputImage' type='image' src='foo'>\n"
            + "<button name='myButton'>foo</button>\n"
            + "</form>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("outer")
    public void writeInManyTimes() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "function doTest(){\n"
            + "    alert(document.getElementById('inner').parentNode.id);\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='doTest()'>\n"
            + "<script>\n"
            + "document.write('<div id=\"outer\">');\n"
            + "document.write('<div id=\"inner\"/>');\n"
            + "document.write('</div>');\n"
            + "</script>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void writeWithSpace() throws Exception {
        final String html = "<html><body><script>\n"
            + "document.write('Hello ');\n"
            + "document.write('World');\n"
            + "</script>\n"
            + "</body></html>";

        final HtmlPage page = loadPage(getBrowserVersion(), html, null);
        assertTrue(page.asText().contains("Hello World"));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void writeWithSplitAnchorTag() throws Exception {
        final String html = "<html><body><script>\n"
            + "document.write(\"<a href=\'start.html\");\n"
            + "document.write(\"\'>\");\n"
            + "document.write('click here</a>');\n"
            + "</script>\n"
            + "</body></html>";

        final HtmlPage page = loadPage(getBrowserVersion(), html, null);
        final List<HtmlAnchor> anchorList = page.getAnchors();
        assertEquals(1, anchorList.size());
        final HtmlAnchor anchor = anchorList.get(0);
        assertEquals("start.html", anchor.getHrefAttribute());
        assertEquals("click here", anchor.asText());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void writeScriptInManyTimes() throws Exception {
        final String html = "<html><head><title>foo</title>\n"
            + "<script>\n"
            + "document.write('<script src=\"script.js\">');\n"
            + "document.write('<' + '/script>');\n"
            + "document.write('<script>alert(\"foo2\");</' + 'script>');\n"
            + "</script>\n"
            + "</head>\n"
            + "<body>\n"
            + "</body></html>";

        final String[] expectedAlerts = {"foo", "foo2"};

        final URL scriptUrl = new URL(URL_FIRST + "script.js");
        final WebClient client = getWebClient();
        final MockWebConnection webConnection = new MockWebConnection();
        client.setWebConnection(webConnection);
        webConnection.setDefaultResponse(html);
        webConnection.setResponse(scriptUrl, "alert('foo');\n", "text/javascript");

        final List<String> collectedAlerts = new ArrayList<String>();
        client.setAlertHandler(new CollectingAlertHandler(collectedAlerts));
        client.getPage(URL_FIRST);

        assertEquals(expectedAlerts, collectedAlerts);
    }

    /**
     * Test for bug 1613119.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "scr1", "scr2"/*, "scr3", "scr4"*/ })
    public void writeAddNodesInCorrectPositions() throws Exception {
        final String html = "<html><head><title>foo</title></head>\n"
            + "<body id=\"theBody\">\n"
            + "<div id='target1'></div>\n"
            + "<script>\n"
            + "document.write(\""
            + "<div>"
            + "  <sc\"+\"ript id='scr1'>document.write('<div id=\\\"div1\\\" />');</s\"+\"cript>"
            + "  <sc\"+\"ript id='scr2'>document.write('<div id=\\\"div2\\\" />');</s\"+\"cript>"
            + "</div>"
            + "\");\n"
 /*           + "document.getElementById('target1').innerHTML = \""
            + "<div>\n"
            + "  <sc\"+\"ript id='scr3'>document.write('<div id=\\\"div3\\\" />');</s\"+\"cript>\n"
            + "  <sc\"+\"ript id='scr4'>document.write('<div id=\\\"div4\\\" />');</s\"+\"cript>\n"
            + "</div>\n"
            + "\";\n"
  */
            + "</script>\n"
            + "<script>\n"
            + "function alertId(obj) { alert(obj != null ? obj.id : 'null'); }\n"
            + "alertId(document.getElementById('div1').previousSibling);\n"
            + "alertId(document.getElementById('div2').previousSibling);\n"
 /*           + "alertId(document.getElementById('div3').previousSibling);\n"
            + "alertId(document.getElementById('div4').previousSibling);\n"
  */
            + "</script>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "www.gargoylesoftware.com", "gargoylesoftware.com" })
    public void domain() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "function doTest(){\n"
            + "    alert(document.domain);\n"
            + "    document.domain = 'gargoylesoftware.com';\n"
            + "    alert(document.domain);\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='doTest()'>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

  /**
    * @throws Exception if the test fails
    */
    @Test
    @Browsers(Browser.FF)
    public void domainMixedCaseNetscape() throws Exception {
        final URL urlGargoyleUpperCase = new URL("http://WWW.GARGOYLESOFTWARE.COM/");

        final String html = "<html><head><title>foo</title><script>\n"
            + "function doTest(){\n"
            + "    alert(document.domain);\n"
            + "    document.domain = 'GaRgOyLeSoFtWaRe.CoM';\n"
            + "    alert(document.domain);\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='doTest()'>\n"
            + "</body></html>";

        final List<String> collectedAlerts = new ArrayList<String>();

        loadPage(getBrowserVersion(), html, collectedAlerts, urlGargoyleUpperCase);

        final String[] expectedAlerts = {"www.gargoylesoftware.com", "gargoylesoftware.com"};
        assertEquals(expectedAlerts, collectedAlerts);
    }

  /**
    * @throws Exception if the test fails
    */
    @Test
    @Alerts(FF = { "www.gargoylesoftware.com", "gargoylesoftware.com" },
            IE = { "www.gargoylesoftware.com", "GaRgOyLeSoFtWaRe.CoM" })
    public void domainMixedCase() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "function doTest(){\n"
            + "    alert(document.domain);\n"
            + "    document.domain = 'GaRgOyLeSoFtWaRe.CoM';\n"
            + "    alert(document.domain);\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='doTest()'>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void domainLong() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "function doTest(){\n"
            + "    alert(document.domain);\n"
            + "    document.domain = 'd4.d3.d2.d1.gargoylesoftware.com';\n"
            + "    alert(document.domain);\n"
            + "    document.domain = 'd1.gargoylesoftware.com';\n"
            + "    alert(document.domain);\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='doTest()'>\n"
            + "</body></html>";

        final String[] expectedAlerts =
        {"d4.d3.d2.d1.gargoylesoftware.com", "d4.d3.d2.d1.gargoylesoftware.com", "d1.gargoylesoftware.com"};

        final List<String> collectedAlerts = new ArrayList<String>();
        loadPage(getWebClient(), html, collectedAlerts, new URL("http://d4.d3.d2.d1.gargoylesoftware.com"));
        assertEquals(expectedAlerts, collectedAlerts);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void domainSetSelf() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "function doTest(){\n"
            + "    alert(document.domain);\n"
            + "    document.domain = 'localhost';\n"
            + "    alert(document.domain);\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='doTest()'>\n"
            + "</body></html>";

        final String[] expectedAlerts = {"localhost", "localhost"};

        final List<String> collectedAlerts = new ArrayList<String>();
        loadPage(getWebClient(), html, collectedAlerts, new URL("http://localhost"));
        assertEquals(expectedAlerts, collectedAlerts);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void domainTooShort() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "function doTest(){\n"
            + "    alert(document.domain);\n"
            + "    document.domain = 'com';\n"
            + "    alert(document.domain);\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='doTest()'>\n"
            + "</body></html>";

        final List<String> collectedAlerts = new ArrayList<String>();
        try {
            loadPage(getWebClient(), html, collectedAlerts);
        }
        catch (final ScriptException ex) {
            return;
        }
        fail();
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void writeFrameRelativeURLMultipleFrameset() throws Exception {
        final String html = "<html><head><title>frameset</title></head>\n"
            + "<script>\n"
            + "    document.write('<frameset><frame src=\"frame.html\"/></frameset>');\n"
            + "</script>\n"
            + "<frameset><frame src='blank.html'/></frameset>\n"
            + "</html>";

        final URL baseURL = new URL("http://base/subdir/");
        final URL framesetURL = new URL(baseURL + "test.html");
        final URL frameURL = new URL(baseURL + "frame.html");
        final URL blankURL = new URL(baseURL + "blank.html");

        final WebClient client = getWebClient();
        final MockWebConnection webConnection = new MockWebConnection();
        webConnection.setResponse(framesetURL, html);
        webConnection.setResponseAsGenericHtml(frameURL, "frame");
        webConnection.setResponseAsGenericHtml(blankURL, "blank");
        client.setWebConnection(webConnection);

        final HtmlPage framesetPage = client.getPage(framesetURL);
        final FrameWindow frame = framesetPage.getFrames().get(0);
        final HtmlPage framePage = (HtmlPage) frame.getEnclosedPage();

        assertNotNull(frame);
        assertEquals(frameURL.toExternalForm(), framePage.getWebResponse().getRequestUrl().toExternalForm());
        assertEquals("frame", framePage.getTitleText());
    }

   /**
    * Test for bug 1185389.
    * @throws Exception if the test fails
    */
    @Test
    @Alerts({ "theBody", "theBody", "theBody" })
    public void writeAddNodesToCorrectParent() throws Exception {
        final String html = "<html><head><title>foo</title></head>\n"
            + "<body id=\"theBody\">\n"
            + "<script>\n"
            + "document.write('<p id=\"para1\">Paragraph #1</p>');\n"
            + "document.write('<p id=\"para2\">Paragraph #2</p>');\n"
            + "document.write('<p id=\"para3\">Paragraph #3</p>');\n"
            + "alert(document.getElementById('para1').parentNode.id);\n"
            + "alert(document.getElementById('para2').parentNode.id);\n"
            + "alert(document.getElementById('para3').parentNode.id);\n"
            + "</script>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

   /**
    * Test for bug 1678826.
    * https://sourceforge.net/tracker/index.php?func=detail&aid=1678826&group_id=47038&atid=448266
    * @throws Exception if the test fails
    */
    @Test
    @Alerts({ "outer", "inner1" })
    public void writeAddNodesToCorrectParent_Bug1678826() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "function doTest(){\n"
            + "    alert(document.getElementById('inner1').parentNode.id);\n"
            + "    alert(document.getElementById('inner2').parentNode.id);\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='doTest()'>\n"
            + "<script>\n"
            + "document.write('<div id=\"outer\">');"
            + "document.write('<br id=\"br1\">');"
            + "document.write('<div id=\"inner1\"/>');"
            + "document.write('<hr id=\"hr1\"/>');"
            + "document.write('<div id=\"inner2\"/>');"
            + "document.write('</div>');"
            + "</script>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

     /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("2")
    public void scriptsArray() throws Exception {
        final String html = "<html><head><script lang='JavaScript'>\n"
            + "    function doTest(){\n"
            + "        alert(document.scripts.length);\n" // This line used to blow up
            + "}\n"
            + "</script></head><body onload='doTest();'>\n"
            + "<script>var scriptTwo = 1;</script>\n"
            + "</body></html> ";

        loadPageWithAlerts(html);
    }

    /**
     * Any document.foo should first look at elements named "foo" before using standard functions.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("FORM")
    public void precedence() throws Exception {
        final String html = "<html><head></head>\n"
            + "<body>\n"
            + "<form name='writeln'>foo</form>\n"
            + "<script>alert(document.writeln.tagName);</script>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(FF = { "not defined" },
            IE = { "true", "1", "about:blank", "about:blank" })
    public void frames() throws Exception {
        final String html = "<html><head><script>\n"
            + "function test(){\n"
            + "  if (document.frames)\n"
            + "  {\n"
            + "    alert(document.frames == window.frames);\n"
            + "    alert(document.frames.length);\n"
            + "    alert(document.frames(0).location);\n"
            + "    alert(document.frames('foo').location);\n"
            + "  }\n"
            + "  else\n"
            + "    alert('not defined');\n"
            + "}\n"
            + "</script></head><body onload='test();'>\n"
            + "<iframe src='about:blank' name='foo'></iframe>\n"
            + "</body></html> ";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(FF = { "true", "false" },
            IE = { "false", "true" })
    public void defaultViewAndParentWindow() throws Exception {
        final String html = "<html><head><script>\n"
            + "function test(){\n"
            + "    alert(document.defaultView == window);\n"
            + "    alert(document.parentWindow == window);\n"
            + "}\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html> ";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "undefined", "123" })
    public void put() throws Exception {
        final String html = "<html><body>\n"
                + "<script>\n"
                + "alert(document.foo);\n"
                + "if (!document.foo) document.foo = 123;\n"
                + "alert(document.foo);\n"
                + "</script>\n"
                + "</form>\n" + "</body>\n" + "</html>";

        loadPageWithAlerts(html);
    }

    /**
     * Tests <tt>document.cloneNode()</tt>.
     * IE specific.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "[object]", "true", "true", "true", "true", "true", "true" })
    @NotYetImplemented
    public void documentCloneNode() throws Exception {
        final String html = "<html><body id='hello' onload='doTest()'>\n"
                + "  <script id='jscript'>\n"
                + "    function doTest() {\n"
                + "      var clone = document.cloneNode(true);\n"
                + "      alert(clone.body);\n"
                + "      alert(clone.body !== document.body);\n"
                + "      alert(clone.getElementById(\"id1\") !== document.getElementById(\"id1\"));\n"
                + "      alert(document.ownerDocument == null);\n"
                + "      alert(clone.ownerDocument == document);\n"
                + "      alert(document.getElementById(\"id1\").ownerDocument === document);\n"
                + "      alert(clone.getElementById(\"id1\").ownerDocument === document);\n"
                + "    }\n"
                + "    function assert(clone, expr, info) {\n"
                + "      if (!eval(expr)) {\n"
                + "        alert('failed assertion: ' + expr + ', info: ' + info);\n"
                + "      }\n"
                + "    }\n"
                + "  </script>\n"
                + "  <div id='id1'>hello</div>\n"
                + "</body>\n" + "</html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "[object]" })
    @Browsers(Browser.IE)
    public void createStyleSheet() throws Exception {
        final String html
            = "<html><head><title>foo</title><script>\n"
            + "var s = document.createStyleSheet('foo.css', 1);\n"
            + "alert(s);\n"
            + "</script></head><body>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void createDocumentFragment() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var fragment = document.createDocumentFragment();\n"
            + "    var textarea = document.getElementById('myTextarea');\n"
            + "    textarea.value += fragment.nodeName + '_';\n"
            + "    textarea.value += fragment.nodeValue + '_';\n"
            + "    textarea.value += fragment.nodeType + '_';\n"
            + "    textarea.value += fragment.parentNode + '_';\n"
            + "    textarea.value += fragment.childNodes.length + '_';\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "<textarea id='myTextarea' cols='40'></textarea>\n"
            + "</body></html>";

        final String expected = "#document-fragment_null_11_null_0_";
        final HtmlPage page = loadPage(html);
        final HtmlTextArea textArea = page.getHtmlElementById("myTextarea");
        assertEquals(expected, textArea.getText());
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Browsers(Browser.FF)
    public void createEvent_FF() throws Exception {
        createEvent_FF("Event", true);
        createEvent_FF("Events", true);
        createEvent_FF("HTMLEvents", true);
        createEvent_FF("Bogus", false);
    }

    private void createEvent_FF(final String eventType, final boolean isSupportedType) throws Exception {
        final String html =
              "<html><head><title>foo</title><script>\n"
            + "var e = document.createEvent('" + eventType + "');\n"
            + "alert(e != null);\n"
            + "alert(typeof e);\n"
            + "alert(e);\n"
            + "alert(e.cancelable);\n"
            + "</script></head><body>\n"
            + "</body></html>";
        final List<String> actual = new ArrayList<String>();
        try {
            final String[] expected = {"true", "object", "[object Event]", "true"};
            createTestPageForRealBrowserIfNeeded(html, expected);
            loadPage(getBrowserVersion(), html, actual);
            assertTrue("Test was expected to fail, but did not: type=" + eventType, isSupportedType);
            assertEquals(expected, actual);
        }
        catch (final Exception e) {
            assertTrue("Test was not expected to fail, but did with message " + e.getMessage() + " for type="
                + eventType, !isSupportedType);
        }
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Browsers(Browser.FF)
    @Alerts({ "true", "true", "[object HTMLDivElement]" })
    public void createEvent_FF_Target() throws Exception {
        final String html =
              "<html>\n"
            + "    <body onload='test()'>\n"
            + "        <div id='d' onclick='alert(event.target)'>abc</div>\n"
            + "        <script>\n"
            + "            function test() {\n"
            + "                var event = document.createEvent('MouseEvents');\n"
            + "                alert(event.target == document);\n"
            + "                event.initMouseEvent('click', true, true, window,\n"
            + "                    1, 0, 0, 0, 0, false, false, false, false, 0, null);\n"
            + "                alert(event.target == document);\n"
            + "                document.getElementById('d').dispatchEvent(event);\n"
            + "            }\n"
            + "        </script>\n"
            + "    </body>\n"
            + "</html>";
        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Browsers(Browser.IE)
    @Alerts({ "true", "object", "[object]" })
    public void createEventObject_IE() throws Exception {
        final String html =
              "<html><head><title>foo</title><script>\n"
            + "var e = document.createEventObject();\n"
            + "alert(e != null);\n"
            + "alert(typeof e);\n"
            + "alert(e);\n"
            + "</script></head><body>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Browsers(Browser.IE)
    @Alerts("BODY")
    public void lementFromPoint() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var e = document.elementFromPoint(-1,-1);\n"
            + "    alert(e.nodeName);\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";
        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("false")
    public void createElementWithAngleBrackets() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var select = document.createElement('<select>');\n"
            + "    alert(select.add == undefined);\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";
        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Browsers(Browser.IE)
    @Alerts({ "false", "mySelect", "0" })
    public void createElementWithHtml() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var select = document.createElement(\"<select id='mySelect'><option>hello</option>\");\n"
            + "    alert(select.add == undefined);\n"
            + "    alert(select.id);\n"
            + "    alert(select.childNodes.length);\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(FF = { "[object StyleSheetList]", "0", "true" },
            IE = { "[object]", "0", "true" })
    public void styleSheets() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    alert(document.styleSheets);\n"
            + "    alert(document.styleSheets.length);\n"
            + "    alert(document.styleSheets == document.styleSheets);\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void queryCommandSupported() throws Exception {
        testHTMLFile("DocumentTest_queryCommandSupported.html");
    }

    /**
     * Various <tt>document.designMode</tt> tests when the document is in the root HTML page.
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(FF = { "off", "off", "on", "on", "on", "off", "off", "off", "off" },
            IE = { "Off", "!", "Off", "Off", "Off", "!", "Off", "Off", "Off", "Off", "Off" })
    public void designMode_root() throws Exception {
        designMode("document");
    }

    /**
     * Various <tt>document.designMode</tt> tests when the document is in an <tt>iframe</tt>.
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(FF = { "off", "off", "on", "on", "on", "off", "off", "off", "off" },
            IE = { "Inherit", "!", "Inherit", "On", "On", "!", "On", "Off", "Off", "Inherit", "Inherit" })
    public void designMode_iframe() throws Exception {
        designMode("window.frames['f'].document");
    }

    private void designMode(final String doc) throws Exception {
        final String html = "<html><body><iframe name='f' id='f'></iframe><script>\n"
            + "var d = " + doc + ";\n"
            + "alert(d.designMode);\n"
            + "try{d.designMode='abc';}catch(e){alert('!');}\n"
            + "alert(d.designMode);\n"
            + "try{d.designMode='on';}catch(e){alert('!');}\n"
            + "alert(d.designMode);\n"
            + "try{d.designMode='On';}catch(e){alert('!');}\n"
            + "alert(d.designMode);\n"
            + "try{d.designMode='abc';}catch(e){alert('!');}\n"
            + "alert(d.designMode);\n"
            + "try{d.designMode='Off';}catch(e){alert('!');}\n"
            + "alert(d.designMode);\n"
            + "try{d.designMode='off';}catch(e){alert('!');}\n"
            + "alert(d.designMode);\n"
            + "try{d.designMode='Inherit';}catch(e){alert('!');}\n"
            + "alert(d.designMode);\n"
            + "try{d.designMode='inherit';}catch(e){alert('!');}\n"
            + "alert(d.designMode);\n"
            + "</script></body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(FF = { "error", "error", "true", "true", "true" },
            IE = { "true", "true", "true", "true", "true" })
    public void queryCommandEnabled() throws Exception {
        final String html = "<html><body onload='x()'><iframe name='f' id='f'></iframe><script>\n"
            + "function x() {\n"
            + "var d = window.frames['f'].document;\n"
            + "try { alert(d.queryCommandEnabled('SelectAll')); } catch(e) { alert('error'); }\n"
            + "try { alert(d.queryCommandEnabled('sElectaLL')); } catch(e) { alert('error'); }\n"
            + "d.designMode='on';\n"
            + "alert(d.queryCommandEnabled('SelectAll'));\n"
            + "alert(d.queryCommandEnabled('selectall'));\n"
            + "alert(d.queryCommandEnabled('SeLeCtALL'));}\n"
            + "</script></body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Minimal test for <tt>execCommand</tt>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("command foo not supported")
    public void execCommand() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    document.designMode = 'On';\n"
            + "    document.execCommand('Bold', false, null);\n"
            + "    try {\n"
            + "      document.execCommand('foo', false, null);\n"
            + "    }\n"
            + "    catch (e) {\n"
            + "      alert('command foo not supported');\n"
            + "    }\n"
            + "    document.designMode = 'Off';\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("[object HTMLHeadingElement]")
    @Browsers(Browser.FF)
    public void evaluate_caseInsensitiveAttribute() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var expr = './/*[@CLASS]';\n"
            + "    var result = document.evaluate(expr, document.documentElement, null, XPathResult.ANY_TYPE, null);\n"
            + "    alert(result.iterateNext());\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "  <h1 class='title'>Some text</h1>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("[object HTMLHtmlElement]")
    @Browsers(Browser.FF)
    public void evaluate_caseInsensitiveTagName() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var expr = '/hTmL';\n"
            + "    var result = document.evaluate(expr, document.documentElement, null, XPathResult.ANY_TYPE, null);\n"
            + "    alert(result.iterateNext());\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "  <h1 class='title'>Some text</h1>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @NotYetImplemented(Browser.FF)
    @Alerts(FF = { "SCRIPT", "TITLE" },
            IE = { "STYLE", "SCRIPT" })
    public void writeStyle() throws Exception {
        final String html = "<html><head><title>foo</title></head>\n"
            + "<body>\n"
            + "<script>\n"
            + "  document.write('<style type=\"text/css\" id=\"myStyle\">');\n"
            + "  document.write('  .nwr {white-space: nowrap;}');\n"
            + "  document.write('</style>');\n"
            + "  document.write('<div id=\"myDiv\">');\n"
            + "  document.write('</div>');\n"
            + "  alert(document.getElementById('myDiv').previousSibling.nodeName);\n"
            + "  alert(document.getElementById('myStyle').previousSibling.nodeName);\n"
            + "</script>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "div1", "null", "2", "1" })
    @Browsers(Browser.FF)
    public void importNode_deep() throws Exception {
        importNode(true);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "div1", "null", "0" })
    @Browsers(Browser.FF)
    public void importNode_notDeep() throws Exception {
        importNode(false);
    }

    private void importNode(final boolean deep) throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var node = document.importNode(document.getElementById('div1'), " + deep + ");\n"
            + "    alert(node.id);\n"
            + "    alert(node.parentNode);\n"
            + "    alert(node.childNodes.length);\n"
            + "    if (node.childNodes.length != 0)\n"
            + "      alert(node.childNodes[0].childNodes.length);\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "  <div id='div1'><div id='div1_1'><div id='div1_1_1'></div></div><div id='div1_2'></div></div>\n"
            + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Verifies that HtmlUnit behaves correctly when a document is missing the <tt>body</tt> tag (it
     * needs to be added once the document has finished loading).
     *
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(FF = { "1: null", "2: null", "3: [object HTMLBodyElement]" },
            IE = { "1: null", "2: [object]", "3: [object]" })
    public void noBodyTag() throws Exception {
        final String html =
              "<html>\n"
            + "  <head>\n"
            + "    <title>Test</title>\n"
            + "    <script>alert('1: ' + document.body);</script>\n"
            + "    <script defer=''>alert('2: ' + document.body);</script>\n"
            + "    <script>window.onload = function() { alert('3: ' + document.body); }</script>\n"
            + "  </head>\n"
            + "</html>";

        loadPageWithAlerts(html);
    }

    /**
     * Verifies that HtmlUnit behaves correctly when an iframe's document is missing the <tt>body</tt> tag (it
     * needs to be added once the document has finished loading).
     *
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(FF = { "1: [object HTMLBodyElement]", "2: [object HTMLBodyElement]" },
            IE = { "1: null", "2: [object]" })
    public void noBodyTag_IFrame() throws Exception {
        final String html =
              "<html>\n"
            + "  <head>\n"
            + "    <title>Test</title>\n"
            + "  </head>\n"
            + "  <body>\n"
            + "    <iframe id='i'></iframe>\n"
            + "    <script>\n"
            + "      alert('1: ' + document.getElementById('i').contentWindow.document.body);\n"
            + "      window.onload = function() {\n"
            + "        alert('2: ' + document.getElementById('i').contentWindow.document.body);\n"
            + "      };\n"
            + "    </script>\n"
            + "  </body>\n"
            + "</html>";

        loadPageWithAlerts(html);
    }

    /**
     * Verifies that the document object has a <tt>fireEvent</tt> method and that it works correctly (IE only).
     *
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(FF = { }, IE = "x")
    public void fireEvent() throws Exception {
        final String html =
              "<html><body>\n"
            + " <span id='s' onclick='\n"
            + "  if(document.fireEvent) {\n"
            + "    document.onkeydown=function(){alert(\"x\")};\n"
            + "    document.fireEvent(\"onkeydown\")\n"
            + "  }\n"
            + " '>abc</span>\n"
            + "</body></html>";

        final List<String> actual = new ArrayList<String>();
        final HtmlPage page = loadPage(getBrowserVersion(), html, actual);
        final HtmlSpan span = page.getHtmlElementById("s");
        span.click();
        assertEquals(getExpectedAlerts(), actual);
    }

    /**
     * Test the value of document.ownerDocument.
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("null")
    public void ownerDocument() throws Exception {
        final String html = "<html><body id='hello' onload='doTest()'>\n"
                + "  <script>\n"
                + "    function doTest() {\n"
                + "      alert(document.ownerDocument);\n"
                + "    }\n"
                + "  </script>\n"
                + "</body>\n" + "</html>";

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Browsers(Browser.IE)
    public void activeElement() throws Exception {
        final String html = "<html><head><script>\n"
            + "  alert(document.activeElement);"
            + "  function test() { \n"
            + "     alert(document.activeElement.id);\n"
            + "     document.getElementById('text2').setActive();\n"
            + "     alert(document.activeElement.id);\n"
            + "  }\n"
            + "</script></head>\n"
            + "<body>\n"
            + "<input id='text1' onclick='test()'>\n"
            + "<input id='text2' onfocus='alert(\"onfocus text2\")'>\n"
            + "</body></html>";
        final List<String> collectedAlerts = new ArrayList<String>();
        final HtmlPage page = loadPage(getBrowserVersion(), html, collectedAlerts);
        final HtmlTextInput text1 = page.getHtmlElementById("text1");
        text1.focus();
        text1.click();
        assertEquals(new String[]{"null", "text1", "onfocus text2", "text2"}, collectedAlerts);
    }

    /**
     * Test for bug 2024729 (we were missing the document.captureEvents(...) method).
     * @throws Exception if the test fails
     */
    @Test
    @Browsers(Browser.FF)
    public void captureEvents() throws Exception {
        final String content = "<html><head><title>foo</title>\n"
            + "<script>\n"
            + "function t() { alert('captured'); }\n"
            + "document.captureEvents(Event.CLICK);\n"
            + "document.onclick = t;\n"
            + "</script></head><body>\n"
            + "<div id='theDiv' onclick='alert(123)'>foo</div>\n"
            + "</body></html>";

        final List<String> collectedAlerts = new ArrayList<String>();
        final HtmlPage page = loadPage(getBrowserVersion(), content, collectedAlerts);
        ((ClickableElement) page.getHtmlElementById("theDiv")).click();

        final String[] expectedAlerts = {"123", "captured"};
        assertEquals(expectedAlerts, collectedAlerts);
    }

}
