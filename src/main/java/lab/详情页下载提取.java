
package lab;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.List;

public class 详情页下载提取 {
    public static void main(String[] args) throws IOException {
        try (WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(false);

            String url = "https://so.gushiwen.org/shiwenv_45c396367f59.aspx";//行宫 的url
            HtmlPage page = webClient.getPage(url);
            HtmlElement body = page.getBody();
            /*
            List<HtmlElement> elements = body.getElementsByAttribute(
                    "div",
                    "class",
                    "contson"
            );//会把所有这样的标签都拿出来。包括三首猜你喜欢

            for (HtmlElement element : elements) {//四首诗
                System.out.println(element);
            }

            System.out.println(elements.get(0).getTextContent().trim());//打印第一首诗的内容
             */                                 //获取它的正文信息

            // 标题
            {
                String xpath = "//div[@class='cont']/h1/text()";
                Object o = body.getByXPath(xpath).get(0);//会拿出所有这样的
                DomText domText = (DomText)o;
                System.out.println(domText.asText());
            }
            //朝代
            {
                String xpath = "//div[@class='cont']/p[@class='source']/a[1]/text()";//第一个a标签
                Object o = body.getByXPath(xpath).get(0);
                DomText domText = (DomText)o;
                System.out.println(domText.asText());
            }
            //作者
            {
                String xpath = "//div[@class='cont']/p[@class='source']/a[2]/text()";
                Object o = body.getByXPath(xpath).get(0);
                DomText domText = (DomText)o;
                System.out.println(domText.asText());
            }
            //正文
            {
                String xpath = "//div[@class='cont']/div[@class='contson']";
                Object o = body.getByXPath(xpath).get(0);
                HtmlElement element = (HtmlElement)o;
                System.out.println(element.getTextContent().trim());//得到文章内容 修剪空格
            }
        }
    }

}
