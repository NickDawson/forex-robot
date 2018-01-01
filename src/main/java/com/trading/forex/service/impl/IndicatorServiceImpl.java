package com.trading.forex.service.impl;

import com.trading.forex.entity.EconomicCalendar;
import com.trading.forex.exceptions.RobotTechnicalException;
import com.trading.forex.model.*;
import com.trading.forex.service.IndicatorService;
import com.trading.forex.utils.CustomList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by wf on 04/19/2017.
 */
@Service
@Slf4j
public class IndicatorServiceImpl implements IndicatorService {


    private String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";


    @Override
    public CustomList<EconomicCalendar> getEconomicCalendarData(Importance importanceFilter) {
        CustomList<EconomicCalendar> calendarDataCustomList = new CustomList();

        try {
            Connection.Response response = Jsoup.connect("https://www.dailyfx.com/calendar?&tz=+1")
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .referrer("http://www.google.com")
                    .timeout(12000)
                    .followRedirects(true)
                    .execute();
            final CustomList<Element> elements = response.parse().select("tr").attr("class", "event cal-background webinar-hover").stream()
                    .filter(element -> !StringUtils.isEmpty(element.attr("data-importance"))).collect(Collectors.toCollection(CustomList::new));
            for (Element element : elements) {
                final Elements child = element.children();
                final String dateStr = child.get(1).ownText();
                if (StringUtils.isEmpty(dateStr)) {
                    continue;
                }
                final Date date = new SimpleDateFormat(DATE_FORMAT).parse(child.get(1).ownText());
                final String event = child.get(3).ownText();
                final Currency currency = Currency.fromValue(event.substring(0, 3));
                if (null == currency) {
                    continue;
                }
                final Importance importance = Importance.fromValue(element.attr("data-importance"));
                if (importanceFilter != null && !importanceFilter.equals(importance)) {
                    continue;
                }
                final Double actual = extractDouble(child.get(5).ownText());
                final Double forecast = extractDouble(child.get(6).ownText());
                final Double previous = extractDouble(child.get(7).ownText());
                calendarDataCustomList.add(EconomicCalendar.builder()
                        .actual(actual)
                        .forecast(forecast)
                        .previous(previous)
                        .importance(importance)
                        .currency(currency)
                        .economicCalendarID(EconomicCalendar.EconomicCalendarID.builder().eventDate(date).event(event).build())
                        .build());
            }

        } catch (IOException | ParseException e) {
            throw new RobotTechnicalException(e);
        }
        return calendarDataCustomList;
    }

    private Double extractDouble(String source) {

        if (TextUtils.isEmpty(source)) {
            return null;
        }

        String number = "0";
        String sign = "";
        int length = source.length();

        boolean cutNumber = false;
        for (int i = 0; i < length; i++) {
            char c = source.charAt(i);
            if (c == '-') {
                sign = String.valueOf(c);
            }
            if (cutNumber) {
                if (Character.isDigit(c) || c == '.' || c == ',') {
                    c = (c == ',' ? '.' : c);
                    number += c;
                } else {
                    cutNumber = false;
                    break;
                }
            } else {
                if (Character.isDigit(c)) {
                    cutNumber = true;
                    number += c;
                }
            }
        }
        return Double.parseDouble(sign + number);
    }


    @Override
    public InvestingDataGroup expertDecision(final Symbol symbol) {
        try {

            Connection.Response response = Jsoup.connect("http://fr.investing.com/currencies/" + symbol.getInvestingValue() + "/")
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .referrer("http://www.google.com")
                    .timeout(12000)
                    .followRedirects(true)
                    .execute();
            for (Element element : response.parse().select("table")) {
                if (element.attributes().get("class").equals("genTbl closedTbl technicalSummaryTbl ")) {
                    InvestingData investingDataMovingAverage = buildInvestingData(element.select("tbody").select("tr").get(0).select("td"));
                    InvestingData investingDataTechIndicator = buildInvestingData(element.select("tbody").select("tr").get(1).select("td"));
                    InvestingData investingDataSum = buildInvestingData(element.select("tbody").select("tr").get(2).select("td"));

                    return InvestingDataGroup.builder()
                            .movingAverage(investingDataMovingAverage)
                            .summary(investingDataSum)
                            .technicalIndicator(investingDataTechIndicator)
                            .symbol(symbol)
                            .build();
                }
            }
            throw new RobotTechnicalException("Cannot found data");
        } catch (Exception e) {
            log.error("Erreur lors e la recuperation tc pour le symbol " + symbol.getIndicatorValue());
            throw new RobotTechnicalException(e);
        }
    }

    @Override
    public Map<Symbol, InvestingTechIndicator> expertDecision(Duration duration) {

        try {
            final Map<Symbol, InvestingTechIndicator> result = new HashMap<>();
            Connection.Response response = Jsoup.connect("https://www.investing.com/technical/indicators")
                    .ignoreContentType(true)
                    .method(Connection.Method.POST)
                    .data("period", String.valueOf(duration.getDuration() * 60))
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .referrer("http://www.google.com")
                    .timeout(12000)
                    .followRedirects(true)
                    .execute();
            final Elements parents = response.parse().getElementsByAttributeValue("name", "toolTable");
            final Elements allCurs = parents.get(0).getElementsByAttributeValue("class", "h3LikeTitle");
            for (Element element : allCurs) {
                boolean flag = false;
                for (Element child : parents.get(0).children()) {
                    if (child.equals(element)) {
                        int begin = child.elementSiblingIndex();
                        Symbol symbol = Symbol.fromIndicatorValue(element.getElementsByIndexEquals(begin).get(0).ownText().replace("/", ""));
                        flag = true;
                        Element datas = parents.get(0).children().get(begin + 3).child(0).child(0);
                        Double buy = extractNumber(datas.child(0).ownText());
                        Double sell = extractNumber(datas.child(1).ownText());
                        Double neutral = extractNumber(datas.child(2).ownText());
                        Decision summary = Decision.fromValue(datas.child(3).select("span").get(0).ownText());
                        Elements table = parents.get(0).children();
                        Elements indsPage1 = table.get(begin + 1).select("tbody").select("tr");
                        Elements indsPage2 = table.get(begin + 2).select("tbody").select("tr");
                        result.put(symbol, InvestingTechIndicator.builder()
                                .buy(buy)
                                .sell(sell)
                                .neutral(neutral)
                                .symbol(symbol)
                                .rsi(build(indsPage1.get(0)))
                                .stoch(build(indsPage1.get(1)))
                                .stochRsi(build(indsPage1.get(2)))
                                .macd(build(indsPage1.get(3)))
                                .adx(build(indsPage1.get(4)))
                                .williamR(build(indsPage1.get(5)))
                                .cci(build(indsPage2.get(0)))
                                .atr(build(indsPage2.get(1)))
                                .highLows(build(indsPage2.get(2)))
                                .ultimateOscilator(build(indsPage2.get(3)))
                                .roc(build(indsPage2.get(4)))
                                .bullBearPower(build(indsPage2.get(5)))
                                .summary(summary)
                                .build()
                        );
                        break;
                    }
                }
                if (!flag) {
                    throw new RobotTechnicalException("Cannot found data");
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Erreur lors e la recuperation pour la pêriod " + duration);
            throw new RobotTechnicalException(e);
        }

    }

    @Override
    public InvestingTechIndicator expertDecision(Symbol symbol, Duration duration) {

        try {
            Connection.Response response = Jsoup.connect("https://www.investing.com/currencies/" + symbol.getInvestingValue() + "-technical")
                    .ignoreContentType(true)
                    .method(Connection.Method.POST)
                    .data("period", String.valueOf(duration.getDuration() * 60), "viewType", "normal")
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .referrer("http://www.google.com")
                    .timeout(12000)
                    .followRedirects(true)
                    .execute();
            final Elements parents = response.parse().getElementsByAttributeValue("class", "genTbl closedTbl technicalIndicatorsTbl smallTbl float_lang_base_1");
            Elements indsPage1 = parents.get(0).children().select("tr");
            Elements datas = indsPage1.get(13).select("span");
            Double buy = extractNumber(datas.get(1).ownText());
            Double sell = extractNumber(datas.get(3).ownText());
            Double neutral = extractNumber(datas.get(5).ownText());
            Decision summary = Decision.fromValue(datas.get(6).select("span").get(0).ownText());
            return InvestingTechIndicator.builder()
                    .buy(buy)
                    .sell(sell)
                    .neutral(neutral)
                    .symbol(symbol)
                    .rsi(build(indsPage1.get(1)))
                    .stoch(build(indsPage1.get(2)))
                    .stochRsi(build(indsPage1.get(3)))
                    .macd(build(indsPage1.get(4)))
                    .adx(build(indsPage1.get(5)))
                    .williamR(build(indsPage1.get(6)))
                    .cci(build(indsPage1.get(7)))
                    .atr(build(indsPage1.get(8)))
                    .highLows(build(indsPage1.get(9)))
                    .ultimateOscilator(build(indsPage1.get(10)))
                    .roc(build(indsPage1.get(11)))
                    .bullBearPower(build(indsPage1.get(12)))
                    .summary(summary)
                    .build();
        } catch (Exception e) {
            log.error("Erreur lors e la recuperation pour la pêriod " + duration);
            throw new RobotTechnicalException(e);
        }

    }

    private InvestingTechIndicator.IndicatorAction build(Element element) {
        Elements elements = element.select("td");
        return InvestingTechIndicator.IndicatorAction.builder()
                .value(Double.valueOf(elements.get(1).ownText()))
                .action(Decision.fromValue(elements.get(2).select("span").get(0).ownText()))
                .build();
    }

    private Double extractNumber(String chaine) {
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(chaine);
        while (m.find()) {
            return Double.valueOf(m.group());
        }
        throw new RobotTechnicalException("Cannot found number in  string" + chaine);
    }


    private InvestingData buildInvestingData(Elements elements) {
        return InvestingData.builder().cinqMinute(converToDecision(elements.get(1).ownText()))
                .quinzeMinute(converToDecision(elements.get(2).ownText()))
                .heure(converToDecision(elements.get(3).ownText()))
                .jour(converToDecision(elements.get(4).ownText()))
                .mensuel(converToDecision(elements.get(5).ownText()))
                .build();
    }

    private Decision converToDecision(String value) {

        switch (value) {
            case "Achat":
                return Decision.BUY;
            case "Achat Fort":
                return Decision.STRONG_BUY;
            case "Vente":
                return Decision.SELL;
            case "Vente Forte":
                return Decision.STRONG_SELL;
            case "Neutre":
                return Decision.NEUTRAL;
            default:
                return Decision.NEUTRAL;
        }
    }

}
