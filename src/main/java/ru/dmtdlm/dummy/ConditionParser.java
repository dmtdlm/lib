package ru.dmtdlm.dummy;

/**
 * Created by dmtdlm on 21.02.2017.
 */
public class ConditionParser
{
    private int i;
    private String s;
    private int max;
    private int bracketFactor;

    private String pad(int l)
    {
        String result = "";
        for (int i = 0; i < l; i++)
            result += "  ";
        return result;
    }

    private interface ConditionInterface
    {
        public void print(int level);
    }

    private class Condition implements ConditionInterface
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
            System.out.println(pad(level) + level + ": " + operation);
            left.print(level + 1);
            //System.out.println(operation);
            right.print(level + 1);
        }
    }

    private class Predicate implements ConditionInterface
    {
        private String predicate;

        public Predicate(String predicate)
        {
            this.predicate = predicate;
        }

        @Override
        public void print(int level)
        {
            System.out.println(pad(level) + level + ": " + predicate);
        }
    }

    private ConditionInterface parseSub(int level)
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

        while (i <= max)
        {
            char c = s.charAt(i);
            char cNext = s.charAt(i + 1);
            String doubleSymb = "" + c + cNext;
            if (!escaped && c == '\\')
                escaped = true;
            else
            {
                if (!escaped && c == '(')
                {
                    if (isAfterSubCondition)
                        throw new IllegalArgumentException("Wrong symbol at " + i);
                    i++;
                    bracketFactor++;
                    current = parseSub(level + 1);
                    isAfterSubCondition = true;
                }
                else if (!escaped && c == ')')
                {
                    bracketFactor--;
                    //if (bracketFactor < 0)
                    //    throw new IllegalArgumentException("Bracket factor < 0: " + bracketFactor);
                    break;
                }
                else if (!escaped && (doubleSymb.equals("&&") || doubleSymb.equals("||")))
                {
                    i++;
                    isAfterSubCondition = false;

                    ConditionInterface conditionInterface = predicateStr.isEmpty() ? current : new Predicate(predicateStr);

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
                }
                else if (c != ' ' || started)
                {
                    if (isAfterSubCondition)
                        throw new IllegalArgumentException("Wrong symbol at " + i);
                    predicateStr += c;
                    started = true;
                }
                escaped = false;
            }

            i++;
        }

        current = predicateStr.isEmpty() ? current : new Predicate(predicateStr);

        if (isRight)
        {
            if (right != null)
                ((Condition)right).right = current;
            else
                right = current;
            return new Condition(left, right, operation);
        }
        else
            return current;

    }

    public void parse(String s)
    {
        i = 0;
        this.s = s + " ";
        this.max = s.length() - 1;
        this.bracketFactor = 0;

        ConditionInterface condition = parseSub(0);

        if (bracketFactor != 0)
            throw new IllegalArgumentException("Bracket factor: " + bracketFactor);

        condition.print(0);

    }
}
