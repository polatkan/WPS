/*
 * Copyright (C) 2013 Christian Autermann
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.github.autermann.wps.matlab.transform;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.n52.matlab.connector.value.MatlabString;
import org.n52.matlab.connector.value.MatlabValue;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;


/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public class URITransformation extends LiteralTransformation {

    @Override
    public MatlabValue transformInput(IData data) {
        if (data.getPayload() instanceof URI) {
            return new MatlabString(((URI) data.getPayload()).toString());
        } else if (data.getPayload() instanceof URL) {
            return new MatlabString(((URL) data.getPayload()).toString());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected IData fromString(String value) {
        try {
            return new LiteralAnyURIBinding(new URI(value));
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
