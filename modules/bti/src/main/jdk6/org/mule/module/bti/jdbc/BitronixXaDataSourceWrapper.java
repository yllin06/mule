/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.bti.jdbc;

import org.mule.api.MuleContext;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.config.i18n.MessageFactory;
import org.mule.module.bti.transaction.TransactionManagerWrapper;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import bitronix.tm.resource.jdbc.PoolingDataSource;

/**
 * Wrapper for bitronix PoolingDataSource that will return wrapped XA connections.
 */
public class BitronixXaDataSourceWrapper implements DataSource, Initialisable
{

    private final PoolingDataSource xaDataSource;
    private final MuleContext muleContext;

    public BitronixXaDataSourceWrapper(PoolingDataSource xaDataSource, MuleContext muleContext)
    {
        this.xaDataSource = xaDataSource;
        this.muleContext = muleContext;
    }

    public int getLoginTimeout() throws SQLException
    {
        return xaDataSource.getLoginTimeout();
    }

    public void setLoginTimeout(int seconds) throws SQLException
    {
        xaDataSource.setLoginTimeout(seconds);
    }

    public PrintWriter getLogWriter() throws SQLException
    {
        return xaDataSource.getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException
    {
        xaDataSource.setLogWriter(out);
    }

    public Connection getConnection() throws SQLException
    {
        return new BitronixConnectionWrapper(xaDataSource.getConnection());
    }

    public Connection getConnection(String username, String password) throws SQLException
    {
        return new BitronixConnectionWrapper(xaDataSource.getConnection(username, password));
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        return null;
    }

    public void close()
    {
        xaDataSource.close();
    }

    public PoolingDataSource getWrappedDataSource()
    {
        return xaDataSource;
    }

    @Override
    public void initialise() throws InitialisationException
    {
        if (!(muleContext.getTransactionManager() instanceof TransactionManagerWrapper))
        {
            throw new InitialisationException(MessageFactory.createStaticMessage("Cannot use a Bitronix data source " +
                                                                                 "pool without using Bitronix transaction manager"), this);
        }
    }
}