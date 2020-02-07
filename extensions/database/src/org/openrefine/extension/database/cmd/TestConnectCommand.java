/*
 * Copyright (c) 2017, Tony Opara
 *        All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this 
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * 
 * Neither the name of Google nor the names of its contributors may be used to 
 * endorse or promote products derived from this software without specific 
 * prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openrefine.extension.database.cmd;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.openrefine.extension.database.DatabaseConfiguration;
import org.openrefine.extension.database.DatabaseService;
import org.openrefine.extension.database.DatabaseServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import org.openrefine.util.ParsingUtilities;



public class TestConnectCommand extends DatabaseCommand {

    private static final Logger logger = LoggerFactory.getLogger("TestConnectCommand");

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	if(!hasValidCSRFToken(request)) {
    		respondCSRFError(response);
    		return;
    	}
        
        DatabaseConfiguration databaseConfiguration = getJdbcConfiguration(request);
        if(logger.isDebugEnabled()) {
            logger.debug("TestConnectCommand::Post::{}", databaseConfiguration); 
        }
        
        
        //ProjectManager.singleton.setBusy(true);
        try {
            
            
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Type", "application/json");
           
            Writer w = response.getWriter();
            JsonGenerator writer = ParsingUtilities.mapper.getFactory().createGenerator(w);
            
            try {
                
                boolean connectionTestResult = DatabaseService.get(databaseConfiguration.getDatabaseType())
                        .testConnection(databaseConfiguration);
                
                response.setStatus(HttpStatus.SC_OK);
                writer.writeStartObject();
                
                writer.writeBooleanField("connectionResult", connectionTestResult);
                writer.writeStringField("code", "ok");
                writer.writeEndObject();
                
            } catch (DatabaseServiceException e) {
                logger.error("TestConnectCommand::Post::DatabaseServiceException::{}", e);
                sendError(HttpStatus.SC_UNAUTHORIZED,response, e);
            } finally {
                writer.flush();
                writer.close();
                w.close();
            }
        } catch (Exception e) {
            logger.error("TestConnectCommand::Post::Exception::{}", e);
            throw new ServletException(e);
        } finally {
            //ProjectManager.singleton.setBusy(false);
        }

        
    }

    
}