package ru.dmtdlm.lib.stringcondition;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dmtdlm on 21.02.2017.
 */
public class StringCondition
{
    private int i;
    private String s;
    private int max;
    private int bracketFactor;
    private ConditionInterface rootCondition = null;
    private Map<String, String> variables = new HashMap<>();
    private Map<String, Boolean> flags = new HashMap<>();

    private StringCondition()
    {

    }

    private String pad(int l)
    {
        String result = "";
        for (int i = 0; i < l; i++)
            result += "  ";
        return result;
    }

    private abstract class ConditionInterface
    {
        protected boolean negative = false;
        public abstract void print(int level);
        public abstract boolean calculate();
    }

    private class Condition extends ConditionInterface
    {
        private ConditionInterface left;
        private ConditionInterface right;
        private String operation;

        public Condition(ConditionInterface left, ConditionInterface right, String operation)
        {
            this.left = left;
            this.right = right;
            this.operation = operation;
        }

        @Override
        public void print(int level)
        {
            System.out.println(pad(level) + (negative ? "!" : "") + level + ": " + operation);
            left.print(level + 1);
            right.print(level + 1);
        }

        @Override
        public boolean calculate()
        {
            boolean result;
            if (operation.equals("&&"))
                result = left.calculate() && right.calculate();
            else
                result = left.calculate() || right.calculate();

            return negative != result;
        }
    }

    private class Predicate extends ConditionInterface
    {
        private Object left;
        private Object right;

        public Predicate(String predicate, boolean negative)
        {
            //cut off end spaces
            predicate = predicate.replaceAll("\\s*$","");

            this.negative = negative;

            Pattern pattern = Pattern.compile("\\s*(\\S+)\\s*(==|!=)\\s*(\\S+)\\s*");
            Matcher matcher = pattern.matcher(predicate);

            if (matcher.matches())
            {
                if (matcher.group(2).equals("!="))
                    this.negative = !this.negative;
                String leftStr = matcher.group(1);
                String rightStr = matcher.group(3);
                left = getPredicatePart(leftStr);
                right = getPredicatePart(rightStr);
            }
            else
            {
                predicate = predicate.replaceAll("\\\\(.)","$1");
                flags.put(predicate, false);
                left = new VariableGetter<Boolean>(flags, predicate);
                right = (new Boolean(true)).toString();
            }
        }

        private Object getPredicatePart(String partStr)
        {
            Object result;
            char first = partStr.charAt(0);
            partStr = partStr.replaceAll("\\\\(.)","$1");
            if (first == '$')
            {
                String key = partStr.substring(1);
                variables.put(key,"");
                result = new VariableGetter<String>(variables, key);
            }
            else
                result = partStr;
            return result;
        }

        @Override
        public void print(int level)
        {
            System.out.println(pad(level) + (negative ? "!" : "") + level + ": " + left.toString() + "==" + right.toString());
        }

        @Override
        public boolean calculate()
        {
            return negative != left.toString().equals(right.toString());
        }

        private class VariableGetter<T>
        {
            private Map<String, T> map;
            private String key;

            public VariableGetter(Map<String, T> map, String key)
            {
                this.map = map;
                this.key = key;
            }

            @Override
            public String toString()
            {
                return map.get(key).toString();
            }
        }
    }

    private ConditionInterface parseSub(int level, boolean negative)
    {
        boolean isRight = false;
        ConditionInterface left = null;
        ConditionInterface right = null;
        ConditionInterface current = null;
        String operation = "";
        String predicateStr = "";

        boolean started = false;
        boolean escaped = false;
        boolean isAfterSubCondition = false;
        boolean subNegative = false;

        while (i <= max)
        {
            char c = s.charAt(i);
            char cNext = s.charAt(i + 1);
            String doubleSymb = "" + c + cNext;
            if (!escaped && c == '\\')
                escaped = true;
            else
            {
                if (!escaped && c == '!' && !started)
                    subNegative = !subNegative;
                else if (!escaped && c == '(')
                {
                    if (isAfterSubCondition)
                        throw new IllegalArgumentException("Wrong symbol at " + i);
                    i++;
                    bracketFactor++;
                    current = parseSub(level + 1, subNegative);
                    subNegative = false;
                    isAfterSubCondition = true;
                }
                else if (!escaped && c == ')')
                {
                    bracketFactor--;
                    break;
                }
                else if (!escaped && (doubleSymb.equals("&&") || doubleSymb.equals("||")))
                {
                    i++;
                    isAfterSubCondition = false;

                    ConditionInterface conditionInterface =
                            predicateStr.isEmpty() ? current : new Predicate(predicateStr, subNegative);

                    if (isRight)
                    {
                        //Если правая часть существует, значит последняя операция была - &&, причем, неполная
                        if (right != null)
                            ((Condition)right).right = conditionInterface;
                        else
                            right = conditionInterface;

                        if (doubleSymb.equals("||"))
                        {
                            left = new Condition(left, right, operation);
                            right = null;
                            operation = doubleSymb;
                        }
                        else if (doubleSymb.equals("&&"))
                        {
                            right = new Condition(right, null, "&&");
                        }
                    }
                    else
                    {
                        left = conditionInterface;
                        operation = doubleSymb;
                    }

                    isRight = true;
                    predicateStr = "";
                    started = false;
                    subNegative = false;
                }
                else if (c != ' ' || started)
                {
                    if (isAfterSubCondition)
                        throw new IllegalArgumentException("Wrong symbol at " + i);
                    //save escaping for predicate parsing
                    if (escaped)
                        predicateStr += "\\";
                    predicateStr += c;
                    started = true;
                }
                escaped = false;
            }

            i++;
        }

        current = predicateStr.isEmpty() ? current : new Predicate(predicateStr, subNegative);

        ConditionInterface result;

        if (isRight)
        {
            if (right != null)
                ((Condition)right).right = current;
            else
                right = current;
            result = new Condition(left, right, operation);
        }
        else
            result = current;

        if (negative)
            result.negative = !result.negative;
        return result;
    }

    private void parse(String s)
    {
        i = 0;
        this.s = s + " ";
        this.max = s.length() - 1;
        this.bracketFactor = 0;

        rootCondition = parseSub(0, false);

        if (bracketFactor != 0)
            throw new IllegalArgumentException("Bracket factor: " + bracketFactor);
    }

    public Set<String> getVariableNames()
    {
        return variables.keySet();
    }

    public void setVariable(String name, String value)
    {
        if (variables.containsKey(name))
            variables.put(name, value);
    }

    public Set<String> getFlagNames()
    {
        return flags.keySet();
    }

    public void setFlag(String name, Boolean value)
    {
        if (flags.containsKey(name))
            flags.put(name, value);
    }


    public void print()
    {
        System.out.println(getFlagNames());
        System.out.println(getVariableNames());

        if (rootCondition != null)
            rootCondition.print(0);
        else
            System.out.println("null");

    }

    public boolean calculate()
    {
        return rootCondition.calculate();
    }

    private void addCondition(String conditionStr, String operation)
    {
        StringCondition stringCondition = new StringCondition();
        stringCondition.parse(conditionStr);

        if (rootCondition == null)
            rootCondition = stringCondition.rootCondition;
        else
            rootCondition = new Condition(rootCondition, stringCondition.rootCondition, operation);

        variables.putAll(stringCondition.variables);
        flags.putAll(stringCondition.flags);
    }

    public void addConditionOr(String conditionStr)
    {
        addCondition(conditionStr, "||");
    }

    public void addConditionAnd(String conditionStr)
    {
        addCondition(conditionStr, "&&");
    }


    public static StringCondition parseCondition(String conditionStr)
    {
        StringCondition stringCondition = new StringCondition();
        stringCondition.parse(conditionStr);
        return stringCondition;
    }

    private static StringCondition parseFile(File file, String operation) throws IOException
    {
        try (FileReader fileReader = new FileReader(file); BufferedReader bufferedReader = new BufferedReader(fileReader))
        {
            String s;
            StringCondition result = new StringCondition();
            while ((s = bufferedReader.readLine()) != null)
            {
                s = s.replaceAll("^\\s*","");
                if (!s.isEmpty() && s.charAt(0) != '#')
                    result.addCondition(s, operation);
            }
            return result;
        }
    }

    public static StringCondition parseFileOr(File file) throws IOException
    {
        return parseFile(file, "||");
    }

    public static StringCondition parseFileAnd(File file) throws IOException
    {
        return parseFile(file, "&&");
    }

}
