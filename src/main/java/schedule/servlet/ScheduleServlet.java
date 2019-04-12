package schedule.servlet;

import schedule.dao.Jdbc;
import schedule.service.Search;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;


/**
 * @return 课表json
 * @Description 根据 学号或姓名 查询课表的接口
 * @Param String content 学号或者姓名
 * @author white matter
 */
@WebServlet("/schedule")
public class ScheduleServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=utf-8");

        String content = request.getParameter("content");

        Jdbc jdbc = new Jdbc();
        Search search = new Search();
        HashMap<String, List<String>> listHashMap = jdbc.findSchedule(content);
        HashMap<String, List> hashMap = jdbc.findDetail(listHashMap);
        response.getWriter().write(search.createFindS(hashMap));

    }
}
