package org.sonatype.plugins.jscoverage;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * @goal stop-jscoverage
 * @phase post-integration-test
 */
public class JSCoverageServerShutdownMojo
    extends AbstractJSCoverageMojo
{

    @Override
    protected void customizeCommandLine( Commandline cmd )
    {
        cmd.createArg().setValue( "--shutdown" );
    }

    @Override
    protected String getExecutable()
    {
        return "jscoverage-server";
    }
}
