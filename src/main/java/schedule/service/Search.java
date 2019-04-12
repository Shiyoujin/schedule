package schedule.service;

import com.mysql.cj.xdevapi.JsonArray;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import schedule.bean.ScheduleBean;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author white matter
 */
public class Search {
    /**
     * @return int 0 1 2 3 分别代表不同的 方法执行后 成功与否的 因素
     * @Description 爬取 某学号的 课表
     * @author white matter
     * @Param List<String>
     */
    public String createFindS(HashMap<String, List> resultMap) {
        JSONObject jsonObjectAll = new JSONObject();
        JSONArray jsonArrayAll = new JSONArray();
        String stuNum = String.valueOf(resultMap.get("student").get(0));
        String stuNam = String.valueOf(resultMap.get("student").get(1));

        Date now = new Date();
        String nowTime = new SimpleDateFormat("yyyy.MM.dd").format(now);
        jsonObjectAll.put("status", "200");
        jsonObjectAll.put("term", "2018-2019学年第2学期");
        jsonObjectAll.put("version", "19.2.25");
        jsonObjectAll.put("nowtime", nowTime);
        jsonObjectAll.put("stuNum", stuNum);
        jsonObjectAll.put("stuNam", stuNam);

        List<ScheduleBean> listBean = resultMap.get("schedule");

        JSONObject jsonObject = new JSONObject();
        String s_messageR = "(?<=(-)).*";
        String dayR = "星期.";
        String lessonR = "(?<=(第)).{2,6}节";
        String weekR = "(?<=(节)).*(?=())";


        for (ScheduleBean scheduleBean : listBean) {
            String s_Time = scheduleBean.getS_time();
            String s_Message = scheduleBean.getS_message();
            String s_Teacher = scheduleBean.getS_teacher();
            String s_Where = scheduleBean.getS_where();
            Matcher matcherDay = Pattern.compile(dayR).matcher(s_Time);
            Matcher matcherLesson = Pattern.compile(lessonR).matcher(s_Time);
            Matcher matcherWeek = Pattern.compile(weekR).matcher(s_Time);

            while (matcherWeek.find()) {
                String week = matcherWeek.group();
                HashMap<String, List<Integer>> hashMap = new HashMap<>(1000);
                hashMap = jsonArrayweekArray(week);
                int j = 0;
                //2周,3-4周,11周,12-13周单周,9-10周双周
                if (matcherDay.find()) {
                    if (matcherLesson.find()) {
                        String lessonArray[] = matcherLesson.group().split("-");
                        jsonObject.put("begin_lesson", lessonArray[0]);
                        //星期5
                        jsonObject.put("day", matcherDay.group());
                        //5-6节
                        jsonObject.put("lesson", matcherLesson.group());
                    }
                }
                Matcher matcherS_message = Pattern.compile(s_messageR).matcher(s_Message);
                if (matcherS_message.find()) {
                    jsonObject.put("course", matcherS_message.group());
                }
                jsonObject.put("teacher", s_Teacher);
                jsonObject.put("classroom", s_Where);
                jsonObject.put("rawWeek", week);
                List<Integer> list = new ArrayList<>();
                list = hashMap.get("weekModel");
                int weekModel = list.get(0);
                if (weekModel == 1) {
                    jsonObject.put("weekModel", "single");
                } else if (weekModel == 2) {
                    jsonObject.put("weekModel", "double");
                } else if (weekModel == 0 || weekModel == 3) {
                    jsonObject.put("weekModel", "all");
                }
                List<Integer> numberList = new ArrayList<>();
                numberList = hashMap.get("week");
                int start = numberList.get(0);
                int numberLength = numberList.size();
                int end = numberList.get(numberLength - 1);
                jsonObject.put("weekBegin", start);
                jsonObject.put("weekEnd", end);
                //这里理论上 应该也是个 正则 ? 如果不是 从数据里面取的话
                jsonObject.put("period", 2);
                JSONArray jsonArray = new JSONArray();
                for (Integer integer : numberList) {
                    jsonArray.add(integer);
                }
                jsonObject.put("week", jsonArray);
            }
            jsonArrayAll.add(jsonObject);
        }
        jsonObjectAll.put("data", jsonArrayAll);
        return jsonObjectAll.toString();
    }


    public HashMap<String, List<Integer>> jsonArrayweekArray(String week) {
        HashMap<String, List<Integer>> hashMap = new HashMap<>(1000);
        // week = 2周,3-4周,11周,12-13周单周,9-10周双周
        String singleR = ".*(?=(周单周))";
        String doubleR = ".*(?=(周双周))";
        String noneR = ".*(?=(周))";

        Matcher matcherSingleModel = Pattern.compile(singleR).matcher(week);
        Matcher matcherDoubleModel = Pattern.compile(doubleR).matcher(week);

        // 1 为单周，2 为双周， 0或3  为all
        int weekModelNum = 0;
        //判断 weekModel 的类型， single double all
        if (matcherSingleModel.find()) {
            weekModelNum++;
        }

        if (matcherDoubleModel.find()) {
            weekModelNum += 2;
        }

        List<Integer> list = new ArrayList<>();
        list.add(weekModelNum);
        //用此list 里面第一个元素的 int 大小 来判断 weekModel
        hashMap.put("weekModel", list);

        String weekArray[] = week.split(",");
        int weekArrayLength = weekArray.length;
        List<Integer> numberList = new ArrayList<>();
        // week = 2周,3-4周,11周,12-13周单周,9-10周双周
        for (int i = 0; i < weekArrayLength; i++) {
            Matcher matcherSingle = Pattern.compile(singleR).matcher(weekArray[i]);
            Matcher matcherDouble = Pattern.compile(doubleR).matcher(weekArray[i]);
            Matcher matcherNone = Pattern.compile(noneR).matcher(weekArray[i]);
            if (matcherSingle.find()) {
                String singleArray[] = matcherSingle.group().split("-");
                int start = Integer.parseInt(singleArray[0]);
                int end = Integer.parseInt(singleArray[1]);
                //单周开头，结尾 必为 单数，同理 双周也是，故不用判断开头数字 单双
                for (; start <= end; start += 2) {
                    numberList.add(start);
                }
            } else if (matcherDouble.find()) {
                String doubleArray[] = matcherDouble.group().split("-");
                int start = Integer.parseInt(doubleArray[0]);
                int end = Integer.parseInt(doubleArray[1]);
                for (; start <= end; start += 2) {
                    numberList.add(start);
                }
            } else if (matcherNone.find()) {
                String noneArray[] = matcherNone.group().split("-");
                if (noneArray.length == 1) {
                    numberList.add(Integer.valueOf(noneArray[0]));
                } else {
                    int start = Integer.parseInt(noneArray[0]);
                    int end = Integer.parseInt(noneArray[1]);
                    for (; start <= end; start++) {
                        numberList.add(start);
                    }
                }
            }
        }
        //将 list里面的 元素 按 从小到大 排序
        Collections.sort(numberList);
        hashMap.put("week", numberList);
        return hashMap;
    }

}
