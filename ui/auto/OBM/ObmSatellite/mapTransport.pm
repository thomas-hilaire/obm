package OBM::ObmSatellite::mapTransport;

require Exporter;
use strict;


sub makeTransportMap {
    my( $daemonRef, $transportMapDesc, $obmDomains ) = @_;
    my $ldapAttributes = $transportMapDesc->{ldap_attibute};
    my %mapEntries;


    if( !defined($obmDomains) || ( ref($obmDomains) ne "ARRAY" ) ) {
        return 1;
    }

    if( !defined($transportMapDesc->{ldap_filter}) || ( $transportMapDesc->{ldap_filter} !~ /<obmDomain>/ ) ) {
        return 1;
    }

    for( my $i=0; $i<=$#{$obmDomains}; $i++ ) {
        $daemonRef->logMessage( "Obtention des informations du domaine : '".$obmDomains->[$i]."'" );

        my $ldapFilter = $transportMapDesc->{ldap_filter};
        $ldapFilter =~ s/<obmDomain>/$obmDomains->[$i]/;

        my @ldapEntries;
        if( &OBM::ObmSatellite::utils::ldapSearch( $daemonRef->{ldap_server}, \@ldapEntries, $ldapFilter, $ldapAttributes ) ) {
            $daemonRef->logMessage( "Echec: lors de l'obtention des informations du domaine '".$obmDomains->[$i]."'" );
            return 1;
        }

        for( my $j=0; $j<=$#ldapEntries; $j++ ) {
            my $entryMailbox = $ldapEntries[$j]->get_value( $ldapAttributes->[0], asref => 1 );
            if( $#{$entryMailbox} != 0 ) {
                next;
            }

            my $mailBoxServer = $ldapEntries[$j]->get_value( $ldapAttributes->[1], asref => 1 );
            if( $#{$mailBoxServer} != 0 ) {
                next;
            }

            $mapEntries{$entryMailbox->[0]} = $mailBoxServer->[0];
        }
    }

    return &OBM::ObmSatellite::utils::writeMap( $transportMapDesc->{postfix_map}, $transportMapDesc->{postfix_map_separator}, \%mapEntries );

}
