package generaloss.rawlist;

import java.util.HashMap;
import java.util.Map;

public class ListGenerator {

    public static void main(String[] args) {
        newClass("ByteList", "byte", "10", "(byte) 0");
        newClass("ShortList", "short", "10", "(short) 0");
        newClass("IntList", "int", "10", "0");
        newClass("LongList", "long", "10", "0L");
        newClass("FloatList", "float", "10", "0F");
        newClass("DoubleList", "double", "10", "0D");
        newClass("BoolList", "boolean", "10", "false");
        newClass("CharList", "char", "10", "(char) 0");
        newClass("StringList", "String", "3", "null");
    }

    public static final String[] NUMBER_PRIMITIVES = {"int", "long", "double", "byte", "char", "short", "float"};

    public static final Map<String, String> PRIMITIVE_BUFFER_MAP = new HashMap<>() {{
        this.put("byte", "ByteBuffer");
        this.put("boolean", "ByteBuffer");
        this.put("short", "ShortBuffer");
        this.put("int", "IntBuffer");
        this.put("long", "LongBuffer");
        this.put("float", "FloatBuffer");
        this.put("double", "DoubleBuffer");
        this.put("char", "CharBuffer");
    }};

    public static final Map<String, String> PRIMITIVE_WRAPPER_MAP = new HashMap<>() {{
        this.put("int", "Integer");
        this.put("long", "Long");
        this.put("double", "Double");
        this.put("boolean", "Boolean");
        this.put("byte", "Byte");
        this.put("char", "Character");
        this.put("short", "Short");
        this.put("float", "Float");
        this.put("String", "String");
    }};

    public static void newClass(String classname, String datatype, String defaultCapacity, String clearValue) {
        final boolean isPrimitive = Character.isLowerCase(datatype.charAt(0));

        final boolean isNumber = ArrayUtils.contains(NUMBER_PRIMITIVES, datatype);

        final String bufferClass = PRIMITIVE_BUFFER_MAP.get(datatype);

        final boolean isString = datatype.equals("String");
        final boolean isBool = datatype.equals("boolean");
        final boolean isChar = datatype.equals("char");

        final boolean hasBufferOps = (bufferClass != null);
        
        final String datatypeWrapper = PRIMITIVE_WRAPPER_MAP.get(datatype);

        // create class
        final String savepath = "src/main/java/generaloss/rawlist/";

        final ClassWriter w = new ClassWriter("generaloss.rawlist", classname, "", "implements Iterable<" + datatypeWrapper + ">");

        // imports
        w.addImport("java.util.function.Function");
        w.addImport("java.util.*");
        if(hasBufferOps) w.addImport("java.nio.*");

        // fields
        w.addField("public static final int DEFAULT_CAPACITY = " + defaultCapacity + ";\n");
        w.addField("private " + datatype + "[] array;");
        w.addField("private int size;");

        // constructors
        w.addConstructor("()",
            "this(DEFAULT_CAPACITY);"
        );
        w.addConstructor("(int capacity)",
            "if(capacity < 0)",
            "   throw new IllegalArgumentException();",
            "this.array = new " + datatype + "[capacity];"
        );
        w.addConstructor("(" + datatype + "... items)",
            "this.size = items.length;",
            "this.array = items;"
        );
        if(isChar){
            w.addConstructor("(String string)",
                "this(string.toCharArray());"
            );
        }
        w.addConstructor("(" + classname + " list)",
            "this.size = list.size;",
            "this.array = list.copyOf();"
        );
        if(hasBufferOps){
            w.addConstructor("(" + bufferClass + " buffer)",
                "this.array = new " + datatype + "[buffer.limit()];",
                "this.addAll(buffer);"
            );
        }
        if(isString){
            w.addConstructor("(Iterable<?> iterable)",
                "this.array = new " + datatype + "[1];",
                "this.addAll(iterable);",
                "this.trim();"
            );
            w.addConstructor("(Collection<?> collection)",
                "this.array = new " + datatype + "[collection.size()];",
                "this.addAll(collection);"
            );
        }else{
            w.addConstructor("(Iterable<" + datatypeWrapper + "> iterable)",
                "this.array = new " + datatype + "[1];",
                "this.addAll(iterable);",
                "this.trim();"
            );
            w.addConstructor("(Collection<" + datatypeWrapper + "> collection)",
                "this.array = new " + datatype + "[collection.size()];",
                "this.addAll(collection);"
            );
        }
        w.addGenericsConstructor("<O>", "(Iterable<O> iterable, Function<O, " + datatypeWrapper + "> func)",
            "this.array = new " + datatype + "[1];",
            "this.addAll(iterable, func);",
            "this.trim();"
        );
        w.addGenericsConstructor("<O>", "(Collection<O> collection, Function<O, " + datatypeWrapper + "> func)",
            "this.array = new " + datatype + "[collection.size()];",
            "this.addAll(collection, func);"
        );
        w.addGenericsConstructor("<O>", "(O[] array, Function<O, " + datatypeWrapper + "> func)",
            "this.array = new " + datatype + "[array.length];",
            "this.addAll(array, func);"
        );

        //
        w.addMethodSplitter();

        // methods
        w.addMethod("public " + datatype + "[] array()",
            "return array;"
        );
        w.addMethod("public " + datatype + "[] arrayTrimmed()",
            "if(array.length == size)",
            "    return array;",
            "return Arrays.copyOf(array, size);"
        );
        w.addMethod("public int size()",
            "return size;"
        );
        w.addMethod("public int capacity()",
            "return array.length;"
        );
        w.addMethod("public int lastIndex()",
            "return Math.max(0, (size - 1));"
        );

        //
        w.addMethodSplitter();

        w.addMethod("private void grow(int minCapacity)",
            "final int oldCapacity = array.length;",
            "if(oldCapacity == 0){",
            "    array = new " + datatype + "[Math.max(minCapacity, DEFAULT_CAPACITY)];",
            "}else{",
            "    final int newCapacity = ArrayUtils.newLength(oldCapacity, minCapacity - oldCapacity, oldCapacity >> 1);",
            "    array = Arrays.copyOf(array, newCapacity);",
            "}"
        );
        w.addMethod("private void grow()",
            "this.grow(size + 1);"
        );

        //
        w.addMethodSplitter();

        w.addMethod("public " + classname + " add(" + datatype + " element)",
            "if(size == array.length)",
            "   this.grow();",
            "",
            "array[size] = element;",
            "size++;",
            "return this;"
        );
        w.addMethod("public " + classname + " add(" + datatype + "... elements)",
            "if(size + elements.length >= array.length)",
            "    this.grow(size + elements.length);",
            "",
            "System.arraycopy(elements, 0, array, size, elements.length);",
            "size += elements.length;",
            "return this;"
        );
        if(datatype.equals("char")) {
            w.addMethod("public " + classname + " add(String string)",
                "if(string == null)",
                "    return this;",
                "return this.add(string.toCharArray());"
            );
        }
        w.addMethod("public " + classname + " add(" + classname + " list)",
            "if(list.size() == list.capacity()){",
            "    this.add(list.array());",
            "}else{",
            "    this.add(list.arrayTrimmed());",
            "}",
            "return this;"
        );
        w.addMethod("public " + classname + " add(int i, " + datatype + " element)",
            "final int minCapacity = Math.max(size, i) + 1;",
            "if(minCapacity >= array.length)",
            "    this.grow(minCapacity);",
            "",
            "if(size != 0 && size >= i)",
            "    System.arraycopy(array, i, array, i + 1, size - i);",
            "",
            "array[i] = element;",
            "",
            "final int growth = (minCapacity - size);",
            "if(growth > 0)",
            "    size += growth;",
            "return this;"
        );
        w.addMethod("public " + classname + " add(int i, " + datatype + "... elements)",
            "if(elements.length == 0)",
            "    return this;",
            "",
            "final int minCapacity = (Math.max(size, i) + elements.length);",
            "if(minCapacity >= array.length)",
            "    this.grow(minCapacity);",
            "",
            "if(size != 0 && size >= i)",
            "    System.arraycopy(array, i, array, i + elements.length, size - i);",
            "System.arraycopy(elements, 0, array, i, elements.length);",
            "",
            "final int growth = (minCapacity - size);",
            "if(growth > 0)",
            "    size += growth;",
            "return this;"
        );
        if(datatype.equals("char")) {
            w.addMethod("public " + classname + " add(int i, String string)",
                "if(string == null)",
                "    return this;",
                "return this.add(i, string.toCharArray());"
            );
        }
        w.addMethod("public " + classname + " addFirst(" + datatype + " element)",
            "return this.add(0, element);"
        );
        w.addMethod("public " + classname + " addFirst(" + datatype + "... elements)",
            "return this.add(0, elements);"
        );

        //
        w.addMethodSplitter();

        if(hasBufferOps){
            if(isBool){
                w.addMethod("public " + classname + " addAll(" + bufferClass + " buffer)",
                    "for(int i = buffer.position(); i < buffer.limit(); i++)",
                    "    this.add(buffer.get(i) == 1);",
                    "return this;"
                );
            }else{
                w.addMethod("public " + classname + " addAll(" + bufferClass + " buffer)",
                    "for(int i = buffer.position(); i < buffer.limit(); i++)",
                    "    this.add(buffer.get(i));",
                    "return this;"
                );
            }
        }
        if(isString){
            w.addMethod("public " + classname + " addAll(Iterable<?> iterable)",
                "for(Object object: iterable)",
                "    this.add(object.toString());",
                "return this;"
            );
            w.addMethod("public " + classname + " addAll(Collection<?> collection)",
                "for(Object object: collection)",
                "    this.add(object.toString());",
                "return this;"
            );
        }else{
            w.addMethod("public " + classname + " addAll(Iterable<" + datatypeWrapper + "> iterable)",
                "for(" + datatypeWrapper + " item: iterable)",
                "    this.add(item);",
                "return this;"
            );
            w.addMethod("public " + classname + " addAll(Collection<" + datatypeWrapper + "> collection)",
                "for(" + datatypeWrapper + " item: collection)",
                "    this.add(item);",
                "return this;"
            );
        }
        w.addMethod("public <O> " + classname + " addAll(Iterable<O> iterable, Function<O, " + datatypeWrapper + "> func)",
            "for(O object: iterable)",
            "    this.add(func.apply(object));",
            "return this;"
        );
        w.addMethod("public <O> " + classname + " addAll(Collection<O> collection, Function<O, " + datatypeWrapper + "> func)",
            "for(O object: collection)",
            "    this.add(func.apply(object));",
            "return this;"
        );
        w.addMethod("public <O> " + classname + " addAll(O[] array, Function<O, " + datatypeWrapper + "> func)",
            "for(O object: array)",
            "    this.add(func.apply(object));",
            "return this;"
        );

        //
        w.addMethodSplitter();

        w.addMethod("public " + classname + " remove(int i, int len)",
            "len = Math.min(len, size - i);",
            "if(len <= 0)",
            "    return this;",
            "",
            "final int j = (i + len);",
            "System.arraycopy(array, j, array, i, (size - j));",
            "",
            "size -= len;",
            "return this;"
        );
        w.addMethod("public " + datatype + " remove(int i)",
            "final " + datatype + " val = this.get(i);",
            "this.remove(i, 1);",
            "return val;"
        );
        w.addMethod("public " + datatype + " removeFirst()",
            "return this.remove(0);"
        );
        w.addMethod("public " + datatype + " removeLast()",
            "return this.remove(this.lastIndex());"
        );
        w.addMethod("public " + datatypeWrapper + " removeFirst(" + datatype + " value)",
            "final int index = this.indexOf(value);",
            "if(index > -1)",
            "    return this.remove(index);",
            "return null;"
        );
        w.addMethod("public " + datatypeWrapper + " removeLast(" + datatype + " value)",
            "final int index = this.lastIndexOf(value);",
            "if(index > -1)",
            "    return this.remove(index);",
            "return null;"
        );

        //
        w.addMethodSplitter();

        w.addMethod("public boolean contains(" + datatype + " element)",
            "return (this.indexOf(element) != -1);"
        );
        w.addMethod("public int indexOf(" + datatype + " element)",
            "return this.indexOfRange(element, 0, size);"
        );
        w.addMethod("public int lastIndexOf(" + datatype + " element)",
            "return this.lastIndexOfRange(element, 0, size);"
        );
        w.addMethod("public int indexOfRange(" + datatype + " element, int start, int end)",
            "for(int i = start; i < end; i++)",
            "    if(array[i]" + (isPrimitive ? " == " : ".equals(") + "element" + (isPrimitive ? "" : ")") + ")",
            "        return i;",
            "return -1;"
        );
        w.addMethod("public int lastIndexOfRange(" + datatype + " element, int start, int end)",
            "for(int i = end - 1; i >= start; i--)",
            "    if(array[i]" + (isPrimitive ? " == " : ".equals(") + "element" + (isPrimitive ? "" : ")") + ")",
            "        return i;",
            "return -1;"
        );

        //
        w.addMethodSplitter();

        w.addMethod("public boolean isEmpty()",
            "return (size == 0);"
        );
        w.addMethod("public boolean isNotEmpty()",
            "return (size != 0);"
        );

        //
        w.addMethodSplitter();

        w.addMethod("public " + classname + " clear()",
            "Arrays.fill(array, 0, size, " + clearValue + ");",
            "size = 0;",
            "return this;"
        );
        w.addMethod("public " + classname + " fill(" + datatype + " value)",
            "Arrays.fill(array, 0, size, value);",
            "return this;"
        );

        //
        w.addMethodSplitter();

        w.addMethod("public " + classname + " trim()",
            "if(array.length == size)",
            "    return this;",
            "array = Arrays.copyOf(array, size);",
            "return this;"
        );
        w.addMethod("public " + classname + " capacity(int newCapacity)",
            "if(newCapacity == 0){",
            "    array = new " + datatype + "[0];",
            "}else{",
            "    array = Arrays.copyOf(array, newCapacity);",
            "}",
            "size = Math.min(size, newCapacity);",
            "return this;"
        );

        //
        w.addMethodSplitter();

        w.addMethod("public " + datatype + " get(int i)",
            "return array[i];"
        );
        w.addMethod("public " + datatype + " getFirst()",
            "return this.get(0);"
        );
        w.addMethod("public " + datatype + " getLast()",
            "return this.get(this.lastIndex());"
        );
        w.addMethod("public " + classname + " set(int i, " + datatype + " newValue)",
            "array[i] = newValue;",
            "return this;"
        );
        w.addMethod("public " + classname + " setFirst(" + datatype + " newValue)",
            "return this.set(0, newValue);"
        );
        w.addMethod("public " + classname + " setLast(" + datatype + " newValue)",
            "return this.set(this.lastIndex(), newValue);"
        );

        //
        w.addMethodSplitter();

        if(isNumber){
            w.addMethod("public " + classname + " elementAdd(int i, " + datatype + " value)",
                "array[i] += value;",
                "return this;"
            );
            w.addMethod("public " + classname + " elementSub(int i, " + datatype + " value)",
                "array[i] -= value;",
                "return this;"
            );
            w.addMethod("public " + classname + " elementMul(int i, " + datatype + " value)",
                "array[i] *= value;",
                "return this;"
            );
            w.addMethod("public " + classname + " elementDiv(int i, " + datatype + " value)",
                "array[i] /= value;",
                "return this;"
            );
        }
        if(isString){
            w.addMethod("public " + classname + " elementAdd(int i, String value)",
                "array[i] += value;",
                "return this;"
            );
            w.addMethod("public " + classname + " elementAdd(int i, char value)",
                    "array[i] += value;",
                    "return this;"
            );
            w.addMethod("public " + classname + " elementTrim(int i)",
                "array[i] = array[i].trim();",
                "return this;"
            );
            w.addMethod("public " + classname + " elementReplace(int i, char oldChar, char newChar)",
                "array[i] = array[i].replace(oldChar, newChar);",
                "return this;"
            );
            w.addMethod("public " + classname + " elementReplace(int i, CharSequence target, CharSequence replacement)",
                "array[i] = array[i].replace(target, replacement);",
                "return this;"
            );
            w.addMethod("public " + classname + " elementReplaceAll(int i, String regex, String replacement)",
                "array[i] = array[i].replaceAll(regex, replacement);",
                "return this;"
            );
            w.addMethod("public " + classname + " elementReplaceFirst(int i, String regex, String replacement)",
                "array[i] = array[i].replaceFirst(regex, replacement);",
                "return this;"
            );
            w.addMethod("public " + classname + " elementToLowerCase(int i)",
                "array[i] = array[i].toLowerCase();",
                "return this;"
            );
            w.addMethod("public " + classname + " elementToUpperCase(int i)",
                "array[i] = array[i].toUpperCase();",
                "return this;"
            );
        }

        //
        w.addMethodSplitter();

        w.addMethod("public " + datatype + "[] copyOf(int offset, int newLength)",
            "final " + datatype + "[] slice = new " + datatype + "[newLength];",
            "System.arraycopy(array, offset, slice, 0, newLength);",
            "return slice;"
        );
        w.addMethod("public " + datatype + "[] copyOf(int newLength)",
            "return this.copyOf(0, newLength);"
        );
        w.addMethod("public " + datatype + "[] copyOf()",
            "return this.copyOf(size);"
        );
        w.addMethod("public " + datatype + "[] copyOfRange(int from, int to)",
            "return this.copyOf(from, to - from);"
        );
        w.addMethod("public " + classname + " copyTo(" + datatype + "[] dst, int offset, int length)",
            "System.arraycopy(array, 0, dst, offset, length);",
            "return this;"
        );
        w.addMethod("public " + classname + " copyTo(" + datatype + "[] dst, int offset)",
            "return this.copyTo(dst, offset, size);"
        );
        w.addMethod("public " + classname + " copyTo(" + datatype + "[] dst)",
            "return this.copyTo(dst, 0);"
        );
        w.addMethod("public " + classname + " copy()",
            "return new " + classname + "(this);"
        );

        //
        w.addMethodSplitter();

        if(isChar){

            w.addMethod("public String getStringOf()",
                "return new String(array);"
            );
            w.addMethod("public String getStringOf(int offset, int length)",
                "final char[] chars = this.copyOf(offset, length);",
                "return new String(chars);"
            );
            w.addMethod("public String getStringOfRange(int from, int to)",
                "final char[] chars = this.copyOfRange(from, to);",
                "return new String(chars);"
            );

            w.addMethodSplitter();
        }

        //
        w.addAnnotatedMethod("@Override", "public String toString()",
            "return Arrays.toString(this.arrayTrimmed());"
        );
        w.addAnnotatedMethod("@Override", "public boolean equals(Object object)",
            "if(this == object)",
            "    return true;",
            "if(object == null || getClass() != object.getClass())",
            "    return false;",
            "final " + classname + " list = (" + classname + ") object;",
            "return (size == list.size && " + (isPrimitive ? "Arrays.equals" : "Objects.deepEquals") + "(array, list.array));"
        );
        w.addAnnotatedMethod("@Override", "public int hashCode()",
            "return Objects.hash(Arrays.hashCode(array), size);"
        );
        w.addAnnotatedMethod("@Override", "public Iterator<" + datatypeWrapper + "> iterator()",
            "return new Iterator<>() {",
            "    private int index;",
            "    @Override",
            "    public boolean hasNext() {",
            "        return (index < size);",
            "    }",
            "    @Override",
            "    public " + datatypeWrapper + " next() {",
            "        return array[index++];",
            "    }",
            "};"
        );

        // write
        w.write(savepath);
    }

}
