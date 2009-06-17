package org.sonatype.plugins.jscoverage;

import org.codehaus.plexus.util.cli.Commandline;

public abstract class AbstractJSCoverageInstrumentMojo
    extends AbstractJSCoverageMojo
{


    /**
     * Assume that all JavaScript files use the given character encoding. The default is ISO-8859-1. Note that if you
     * use the --proxy option, the character encoding will be determined from the charset parameter in the Content-Type
     * HTTP header.
     *
     * @parameter expression="${jscoverage.encoding}"
     */
    private String encoding;

    /**
     * Use the specified JavaScript version; valid values for VERSION are 1.0, 1.1, 1.2, ..., 1.8, or ECMAv3
     *
     * @parameter expression="${jscoverage.js-version}"
     */
    private String javascriptVersion;


    /**
     * Do not instrument JavaScript code.
     *
     * @parameter
     */
    private String[] doNotInstrument;

    protected void customizeCommandLine( Commandline cmd )
    {
        if ( getLog().isDebugEnabled() )
        {
            cmd.createArg().setValue( "--verbose" );
        }

        if ( javascriptVersion != null )
        {
            cmd.createArg().setValue( "--js-version=" + javascriptVersion );
        }

        if ( doNotInstrument != null )
        {
            for ( String noInstrument : doNotInstrument )
            {
                cmd.createArg().setValue( "--no-instrument=" + noInstrument );
            }
        }

        if ( encoding != null )
        {
            cmd.createArg().setValue( "--encoding==" + encoding );
        }

    }
}
