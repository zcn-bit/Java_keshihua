package lab;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.Html;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import javax.xml.bind.Element;
import java.io.IOException;
import java.util.List;

public class 列表页下载提取Demo {
    public static void main(String[] args) throws IOException {
        try (WebClient webClient = new WebClient(BrowserVersion.CHROME)) { //浏览器版本
            webClient.getOptions().setJavaScriptEnabled(false);//得到选择，得到期权
            webClient.getOptions().setCssEnabled(false);
            String url = "https://so.gushiwen.org/gushi/tangshi.aspx";//
            HtmlPage page = webClient.getPage(url);//超文本编辑页码   请求列表页（唐诗三百首）
            HtmlElement body = page.getBody();
            List<HtmlElement> elements = body.getElementsByAttribute(//属性，特质
                    "div",
                    "class",
                    "typecont"
            );//【五言绝句，七言律诗。。。】

            int count = 0;
            for (HtmlElement element : elements) {
                List<HtmlElement> aElements = element.getElementsByTagName("a");//a标签   就是一首首诗
                for (HtmlElement a : aElements) {
                    System.out.println(a.getAttribute("href"));//一首诗的url
                    count++;//得到了一首诗
                }
            }
            System.out.println(count);//唐诗三百首 下的320首诗
        }
    }
}
