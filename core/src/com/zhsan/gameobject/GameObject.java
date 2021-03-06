package com.zhsan.gameobject;

import com.zhsan.gamecomponents.GlobalStrings;
import com.zhsan.lua.LuaAI;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Peter on 17/3/2015.
 */
public interface GameObject {

    public String getName();

    public int getId();

    public String getAiTags();

    public GameObject setAiTags(String aiTags);

    public default Object getField(String fname) {
        return getField(fname, null);
    }

    public default Object getField(String fname, GameObject context) {
        switch (fname) {
            case "Id":
                return getId();
            case "Name":
                return getName();
            default:
                if (context != null) {
                    try {
                        List<Method> candidates = new ArrayList<>();
                        for (Method m : this.getClass().getMethods()) {
                            if (m.getName().equals("get" + fname) &&
                                    m.getParameterTypes().length == 1 && GameObject.class.isAssignableFrom(m.getParameterTypes()[0])) {
                                candidates.add(m);
                            }
                        }
                        if (candidates.size() == 1) {
                            return candidates.get(0).invoke(this, context);
                        } else {
                            throw new NoSuchMethodException("There must be exactly one method with one GameObject argument." + candidates);
                        }
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        // fall to next case
                    }
                }
                try {
                    Method m = this.getClass().getMethod("get" + fname);
                    return m.invoke(this);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    return GlobalStrings.getString(GlobalStrings.Keys.NO_CONTENT);
                }
        }
    }

    public default String getFieldString(String name) {
        return getFieldString(name, true);
    }

    public default String getFieldString(String name, boolean round) {
        return getFieldString(name, round, null);
    }

    public default String getFieldString(String name, boolean round, GameObject context) {
        Object o = getField(name, context);
        if (o == null) {
            return GlobalStrings.getString(GlobalStrings.Keys.NO_CONTENT);
        } else if (o instanceof Float) {
            return round ? Long.toString(Math.round((float) o)) : Float.toString((Float) o);
        } else if (o instanceof GameObject) {
            return ((GameObject) o).getName();
        } else {
            return Objects.toString(o);
        }
    }

    public default boolean satisfyMethod(String fname) {
        try {
            String actualName = fname.substring(0, 1).toLowerCase() + fname.substring(1);
            Method m = this.getClass().getMethod(actualName);
            return (boolean) m.invoke(this);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassCastException e) {
            return false;
        }
    }

}
