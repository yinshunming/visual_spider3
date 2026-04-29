package com.example.visualspider.service;

import com.example.visualspider.entity.SpiderField;
import com.example.visualspider.entity.SpiderField.ExtractType;
import com.example.visualspider.entity.SpiderField.FieldType;
import com.example.visualspider.entity.SpiderField.SelectorType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.helper.W3CDom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内容页面字段提取器
 * 使用Jsoup进行HTML解析，支持CSS和XPath选择器
 */
@Component
public class ContentPageExtractor {

    private static final Logger log = LoggerFactory.getLogger(ContentPageExtractor.class);
    private static final int TIMEOUT_MS = 30000;

    /**
     * 获取内容页面
     * @param url 页面URL
     * @return Document对象
     * @throws IOException 网络异常
     */
    public Document fetchContentPage(String url) throws IOException {
        log.info("Fetching content page: {}", url);
        return Jsoup.connect(url)
                .timeout(TIMEOUT_MS)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .get();
    }

    /**
     * 从Document中提取多个字段
     * @param doc Document对象
     * @param fields 字段配置列表
     * @return 字段名-值的映射
     */
    public Map<String, Object> extract(Document doc, List<SpiderField> fields) {
        Map<String, Object> result = new HashMap<>();
        if (doc == null || fields == null) {
            return result;
        }

        for (SpiderField field : fields) {
            Object value = extractField(doc, field);
            result.put(field.getFieldName(), value);
        }
        return result;
    }

    /**
     * 从Document中提取单个字段
     * @param doc Document对象
     * @param field 字段配置
     * @return 提取的值
     */
    public Object extractField(Document doc, SpiderField field) {
        if (doc == null || field == null) {
            return getDefaultValue(field);
        }

        try {
            Elements elements = selectElements(doc, field);
            if (elements.isEmpty()) {
                log.debug("No elements found for field: {}", field.getFieldName());
                return getDefaultValue(field);
            }

            Element element = elements.first();
            if (element == null) {
                return getDefaultValue(field);
            }

            Object value = extractValue(element, field);
            return resolveValue(value, field, doc);

        } catch (Exception e) {
            log.error("Failed to extract field: {}, error: {}", field.getFieldName(), e.getMessage());
            return getDefaultValue(field);
        }
    }

    /**
     * 根据选择器类型选择元素
     */
    private Elements selectElements(Document doc, SpiderField field) {
        String selector = field.getSelector();
        if (selector == null || selector.isBlank()) {
            return new Elements();
        }

        SelectorType selectorType = field.getSelectorType();
        if (selectorType == SelectorType.XPATH) {
            return selectByXPath(doc, selector);
        } else {
            return doc.select(selector);
        }
    }

    /**
     * 通过XPath选择元素
     */
    private Elements selectByXPath(Document doc, String xpath) {
        try {
            W3CDom w3cDom = new W3CDom();
            org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(doc);
            XPath xPath = XPathFactory.newInstance().newXPath();
            org.w3c.dom.NodeList nodeList = (org.w3c.dom.NodeList) xPath.evaluate(
                    xpath, w3cDoc, XPathConstants.NODESET);

            Elements elements = new Elements();
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node w3cNode = nodeList.item(i);
                if (w3cNode instanceof org.w3c.dom.Element) {
                    org.w3c.dom.Element w3cElement = (org.w3c.dom.Element) w3cNode;
                    String html = getW3CNodeHtml(w3cElement);
                    if (html != null && !html.isBlank()) {
                        Element jsoupElement = Jsoup.parseBodyFragment(html).body().children().first();
                        if (jsoupElement != null) {
                            elements.add(jsoupElement);
                        }
                    }
                }
            }
            return elements;
        } catch (Exception e) {
            log.error("XPath evaluation failed: {}, xpath: {}", e.getMessage(), xpath);
            return new Elements();
        }
    }

    /**
     * 获取W3C节点的HTML内容
     */
    private String getW3CNodeHtml(org.w3c.dom.Element element) {
        if (element == null) {
            return null;
        }
        try {
            javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
            javax.xml.transform.Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new javax.xml.transform.dom.DOMSource(element), new javax.xml.transform.stream.StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            log.warn("Failed to serialize W3C element: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从元素中提取值
     */
    private Object extractValue(Element element, SpiderField field) {
        ExtractType extractType = field.getExtractType();
        if (extractType == null) {
            extractType = ExtractType.text;
        }

        return switch (extractType) {
            case text -> element.text();
            case html -> element.html();
            case attr -> {
                String attrName = field.getAttrName();
                if (attrName != null && !attrName.isBlank()) {
                    yield element.attr(attrName);
                } else {
                    yield element.text();
                }
            }
        };
    }

    /**
     * 处理特殊类型的值（如图片和链接需要解析相对URL）
     */
    private Object resolveValue(Object value, SpiderField field, Document doc) {
        if (value == null || value.toString().isBlank()) {
            return getDefaultValue(field);
        }

        FieldType fieldType = field.getFieldType();
        String stringValue = value.toString();

        if (fieldType == FieldType.image || fieldType == FieldType.link) {
            return resolveUrl(stringValue, doc);
        }

        return stringValue;
    }

    /**
     * 将相对URL解析为绝对URL
     */
    private String resolveUrl(String url, Document doc) {
        if (url == null || url.isBlank()) {
            return url;
        }

        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }

        Element baseElement = doc.selectFirst("base[href]");
        String baseUrl = null;
        if (baseElement != null) {
            baseUrl = baseElement.attr("href");
        }

        if (baseUrl != null && !baseUrl.isBlank()) {
            return resolveRelativeUrl(url, baseUrl);
        }

        String docUrl = doc.location();
        if (docUrl != null && !docUrl.isBlank()) {
            return resolveRelativeUrl(url, docUrl);
        }

        return url;
    }

    /**
     * 解析相对URL
     */
    private String resolveRelativeUrl(String relativeUrl, String baseUrl) {
        try {
            if (relativeUrl.startsWith("//")) {
                return "https:" + relativeUrl;
            }
            if (relativeUrl.startsWith("/")) {
                java.net.URL base = new java.net.URL(baseUrl);
                return new java.net.URL(base, relativeUrl).toString();
            }
            java.net.URL base = new java.net.URL(baseUrl);
            java.net.URL resolved = new java.net.URL(base, relativeUrl);
            return resolved.toString();
        } catch (Exception e) {
            log.warn("Failed to resolve URL: {} relative to base: {}", relativeUrl, baseUrl);
            return relativeUrl;
        }
    }

    /**
     * 获取默认值
     */
    private Object getDefaultValue(SpiderField field) {
        if (field == null) {
            return null;
        }
        String defaultValue = field.getDefaultValue();
        return defaultValue != null ? defaultValue : null;
    }
}