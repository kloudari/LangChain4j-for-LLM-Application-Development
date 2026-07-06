package com.coursera.langchain.chapter6;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.time.LocalDate;

/**
 * Small hand-crafted tools for the chapter 6 "custom tools" agent demo:
 * today's date, text statistics, temperature conversion, and string
 * reversal. The calculator tool lives separately in {@link CalculatorTool}
 * so it can be reused by {@link Agents} too.
 */
public class CustomTools {

    @Tool("""
            Returns today's date, use this for any question related to \
            knowing today's date. The input is ignored -- this tool always \
            returns today's date; any date arithmetic should happen outside \
            it.""")
    public String time(@P("ignored, pass an empty string") String ignored) {
        return LocalDate.now().toString();
    }

    @Tool("Returns word count and character count for the given text. Input should be the text to analyse.")
    public String textStats(@P("the text to analyse") String text) {
        int words = text.isBlank() ? 0 : text.trim().split("\\s+").length;
        int chars = text.length();
        int charsNoSpaces = text.replace(" ", "").length();
        return "Words: " + words + ", Characters (with spaces): " + chars
                + ", Characters (without spaces): " + charsNoSpaces;
    }

    @Tool("Convert a temperature from Celsius to Fahrenheit. Input should be a numeric string, e.g. '100'.")
    public String celsiusToFahrenheit(@P("the Celsius value") String celsius) {
        try {
            double c = Double.parseDouble(celsius);
            double f = c * 9 / 5 + 32;
            return c + "\u00b0C = " + f + "\u00b0F";
        } catch (NumberFormatException e) {
            return "Invalid input: please provide a numeric value.";
        }
    }

    @Tool("Convert a temperature from Fahrenheit to Celsius. Input should be a numeric string, e.g. '212'.")
    public String fahrenheitToCelsius(@P("the Fahrenheit value") String fahrenheit) {
        try {
            double f = Double.parseDouble(fahrenheit);
            double c = (f - 32) * 5 / 9;
            return f + "\u00b0F = " + Math.round(c * 100.0) / 100.0 + "\u00b0C";
        } catch (NumberFormatException e) {
            return "Invalid input: please provide a numeric value.";
        }
    }

    @Tool("Reverse the characters in a string. Input should be the string to reverse.")
    public String reverseString(@P("the string to reverse") String text) {
        return new StringBuilder(text).reverse().toString();
    }
}
