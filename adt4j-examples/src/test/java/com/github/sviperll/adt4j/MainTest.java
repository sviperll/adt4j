/*
 * Copyright (c) 2015, Victor Nazarov <asviraspossible@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation and/or
 *     other materials provided with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.sviperll.adt4j;

import com.github.sviperll.adt4j.examples.ComparableList;
import com.github.sviperll.adt4j.examples.GroupName;
import com.github.sviperll.adt4j.examples.User;
import com.github.sviperll.adt4j.examples.UserKey;
import com.github.sviperll.adt4j.examples.UserVisitor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class MainTest {

    public MainTest() {
    }

    @Test
    public void testVisitor() throws Exception {
        User<String> user = User.<String>valueOf(UserKey.valueOf(1), ComparableList.<String>empty(), "Victor", GroupName.valueOf("group1"));
        user.accept(new UserVisitor<String, Void> () {

            @Override
            public Void valueOf(UserKey key, ComparableList<String> list, String name, GroupName group) {
                assertEquals(UserKey.valueOf(1), key);
                assertEquals(ComparableList.<String>empty(), list);
                assertEquals("Victor", name);
                assertEquals("group1", group.name());
                return null;
            }
        });

    }

    @Test
    public void testGetters() throws Exception {
        User<String> user = User.<String>valueOf(UserKey.valueOf(1), ComparableList.<String>empty(), "Victor", GroupName.valueOf("group1"));
        assertEquals(UserKey.valueOf(1), user.key());
        assertEquals(ComparableList.<String>empty(), user.list());
        assertEquals("Victor", user.name());
    }

    @Test
    public void testUpdater() throws Exception {
        User<String> user = User.<String>valueOf(UserKey.valueOf(1), ComparableList.<String>empty(), "Victor", GroupName.valueOf("group1"));
        User<String> user1 = user.withName("Peter");

        assertEquals(UserKey.valueOf(1), user.key());
        assertEquals(ComparableList.<String>empty(), user.list());
        assertEquals("Victor", user.name());

        assertEquals(UserKey.valueOf(1), user1.key());
        assertEquals(ComparableList.<String>empty(), user1.list());
        assertEquals("Peter", user1.name());
    }

    @Test
    public void testEquals() {
        User<String> user = User.<String>valueOf(UserKey.valueOf(1), ComparableList.<String>empty(), "Victor", GroupName.valueOf("group1"));
        User<String> user1 = User.<String>valueOf(UserKey.valueOf(1), ComparableList.<String>empty(), "Victor", GroupName.valueOf("group1"));
        assertTrue("user.equals(user1)", user.equals(user1));
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        UserKey userKey1 = UserKey.valueOf(1);
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteArray);
        outputStream.writeObject(userKey1);

        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(byteArray.toByteArray()));
        UserKey userKey2 = (UserKey)inputStream.readObject();
        assertTrue("userKey1.equals(userKey2)", userKey1.equals(userKey2));
    }

    @Test
    public void testComparable() throws IOException, ClassNotFoundException {
        ComparableList<String> list1 = ComparableList.<String>empty();
        ComparableList<String> list2 = ComparableList.<String>empty();

        assertTrue("list1.equals(list2)", list1.equals(list2));
        assertTrue("list1.compareTo(list2) == 0", list1.compareTo(list2) == 0);

        list1 = ComparableList.<String>empty();
        list2 = ComparableList.prepend("a", ComparableList.<String>empty());

        assertFalse("list1.equals(list2)", list1.equals(list2));
        assertTrue("list1.compareTo(list2) < 0", list1.compareTo(list2) < 0);
        assertTrue("list2.compareTo(list1) > 0", list2.compareTo(list1) > 0);

        list1 = ComparableList.prepend("a", ComparableList.<String>empty());
        list2 = ComparableList.prepend("a", ComparableList.<String>empty());

        assertTrue("list1.equals(list2)", list1.equals(list2));
        assertTrue("list1.compareTo(list2) == 0", list1.compareTo(list2) == 0);

        list1 = ComparableList.prepend("a", ComparableList.<String>empty());
        list2 = ComparableList.prepend("b", ComparableList.<String>empty());

        assertFalse("list1.equals(list2)", list1.equals(list2));
        assertTrue("list1.compareTo(list2) < 0", list1.compareTo(list2) < 0);
        assertTrue("list2.compareTo(list1) > 0", list2.compareTo(list1) > 0);

        list1 = ComparableList.prepend("a", ComparableList.<String>empty());
        list2 = ComparableList.prepend("a", ComparableList.prepend("a", ComparableList.<String>empty()));

        assertFalse("list1.equals(list2)", list1.equals(list2));
        assertTrue("list1.compareTo(list2) < 0", list1.compareTo(list2) < 0);
        assertTrue("list2.compareTo(list1) > 0", list2.compareTo(list1) > 0);
    }
}
