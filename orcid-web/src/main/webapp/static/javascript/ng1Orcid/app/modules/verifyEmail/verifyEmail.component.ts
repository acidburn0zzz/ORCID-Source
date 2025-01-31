declare var getBaseUri: any;

import { NgForOf, NgIf } 
    from '@angular/common'; 

import { AfterViewInit, Component, OnDestroy, OnInit } 
    from '@angular/core';

import { Observable } 
    from 'rxjs/Rx';

import { Subject } 
    from 'rxjs/Subject';

import { Subscription }
    from 'rxjs/Subscription';

import { AccountService } 
    from '../../shared/account.service.ts';

import { EmailService } 
    from '../../shared/email.service.ts';

import { ModalService } 
    from '../../shared/modal.service.ts'; 

@Component({
    selector: 'verify-email-ng2',
    template:  scriptTmpl("verify-email-ng2-template")
})
export class VerifyEmailComponent implements AfterViewInit, OnDestroy, OnInit {
    private ngUnsubscribe: Subject<void> = new Subject<void>();
    private subscription: Subscription;

    emailSent: any;
    loading: any;
    emailsPojo: any;
    primaryEmail: string;

    constructor( 
        private accountService: AccountService,
        private emailService: EmailService,
        private modalService: ModalService
    ) {
        this.emailSent = false;
        this.loading = true;
        this.emailsPojo = {};
        this.primaryEmail = '';
    }

    closeColorBox(): void {
        this.accountService.delayVerifyEmail()
        .takeUntil(this.ngUnsubscribe)
        .subscribe(
            data => {
 
            },
            error => {
                //console.log('getWebsitesFormError', error);
            } 
        );
        //$.colorbox.close();
    };

    getEmails(): void {

        this.emailService.getEmails()
        .takeUntil(this.ngUnsubscribe)
        .subscribe(
            data => {

                let primeVerified = false;
                this.emailsPojo = data;

                for (var i in this.emailsPojo.emails) {
                    if (this.emailsPojo.emails[i].primary  == true) {
                        this.primaryEmail = this.emailsPojo.emails[i].value;
                        if (this.emailsPojo.emails[i].verified) {
                            primeVerified = true;
                        }
                    };
                };

                if ( primeVerified == false 
                    && getBaseUri().indexOf("sandbox") == -1 
                ) {
                    /*
                    colorboxHtml = $compile($('#verify-email-modal').html())($scope);
                    $.colorbox({
                        html : colorboxHtml,
                        escKey:false,
                        overlayClose:false,
                        transition: 'fade',
                        close: '',
                        scrolling: false
                    });
                    $.colorbox.resize({width:"500px"});
                    */
                };
                this.loading = false;
            },
            error => {
                //console.log('getEmails', error);
            } 
        );
        /*
        $scope.getEmails = function() {
                $.ajax({
                    url: getBaseUri() + '/account/emails.json',
                    // type: 'POST',
                    // data: $scope.emailsPojo,
                    dataType: 'json',
                    success: function(data) {
                        var configuration = initialConfigService.getInitialConfiguration();
                        var colorboxHtml = null;
                        var primeVerified = false;

                        $scope.verifiedModalEnabled = configuration.showModalManualEditVerificationEnabled;
                        $scope.emailsPojo = data;
                        $scope.$apply();

                        for (var i in $scope.emailsPojo.emails) {
                            if ($scope.emailsPojo.emails[i].primary  == true) {
                                $scope.primaryEmail = $scope.emailsPojo.emails[i].value;
                                if ($scope.emailsPojo.emails[i].verified) {
                                    primeVerified = true;
                                }
                            };
                        };

                        if ( primeVerified == false 
                            && getBaseUri().indexOf("sandbox") == -1 
                        ) {
                            colorboxHtml = $compile($('#verify-email-modal').html())($scope);
                            $scope.$apply();
                            $.colorbox({
                                html : colorboxHtml,
                                escKey:false,
                                overlayClose:false,
                                transition: 'fade',
                                close: '',
                                scrolling: false
                            });
                            $.colorbox.resize({width:"500px"});
                        };
                        $scope.loading = false;
                        $scope.$apply();
                    }
                }).fail(function() {
                    // something bad is happening!
                    //console.log("error with multi email");
                });
            };

        */
    }

    verifyEmail(): void {
        this.emailService.verifyEmail()
        .takeUntil(this.ngUnsubscribe)
        .subscribe(
            data => {
                
            },
            error => {
                //console.log('getEmails', error);
            } 
        );
        this.modalService.notifyOther({action:'close', moduleId: 'modalemailunverified'});
        this.modalService.notifyOther({action:'open', moduleId: 'emailSentConfirmation'});
    }


    //Default init functions provided by Angular Core
    ngAfterViewInit() {
        //Fire functions AFTER the view inited. Useful when DOM is required or access children directives
    };

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    };

    ngOnInit() {
        this.getEmails();
    };

}



/*
declare var $: any;
declare var colorbox: any;
declare var getBaseUri: any;

import * as angular from 'angular';
import {NgModule} from '@angular/core';

// This is the Angular 1 part of the module
export const VerifyEmailCtrl = angular.module('orcidApp').controller(
    'VerifyEmailCtrl', 
    [
        '$compile', 
        '$scope', 
        'emailSrvc', 
        'initialConfigService', 
        function (
            $compile, 
            $scope, 
            emailSrvc, 
            initialConfigService
        ) {

            

            

            
            
            
        }
    ]
);

// This is the Angular 2 part of the module
@NgModule({})
export class VerifyEmailCtrlNg2Module {}
*/