/*************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2012 UBI/HULTIG All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 3 only ("GPL"). You may not use this
 * file except in compliance with the License. You can obtain a copy of
 * the License at http://www.gnu.org/licenses/gpl.txt. See the License
 * for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the software, include this License Header Notice
 * in each file. If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 *
 *       "Portions Copyrighted [year] [name of copyright owner]"
 *************************************************************************
 */
package hultig.util;

/**
 * Defines the assignment or conection value between two
 * entities.
 * 
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: UBI/HULTIG</p>
 *
 * @author JPC
 * @version 1.0
 */
public interface AssignFunction<T>
{
    /**
     * The assignment value between @param x and @param y
     * will be defined here.
     * @param x T
     * @param y T
     * @return double
     */
    public double value(T x, T y);
}
