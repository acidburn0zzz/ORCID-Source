declare var $: any;
declare var colorbox: any;
declare var getBaseUri: any;

//Import all the angular components

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

import { AdminDelegatesService } 
    from '../../shared/adminDelegates.service.ts'; 


@Component({
    selector: 'find-ids-ng2',
    template:  scriptTmpl("find-ids-ng2-template")
})
export class FindIdsComponent implements AfterViewInit, OnDestroy, OnInit {
    private ngUnsubscribe: Subject<void> = new Subject<void>();
    
    emailIdsMap: any;
    emails: any;
    showSection: any;
    profileList: any;

    constructor(
        private adminDelegatesService: AdminDelegatesService
    ) {
        this.emailIdsMap = {};
        this.emails = "";
        this.showSection = false;
        this.profileList = {};
    }

    closeModal(): void {
        $.colorbox.close();
    };

    findIds(): void {
        this.adminDelegatesService.findIds( this.emails )
        .takeUntil(this.ngUnsubscribe)
        .subscribe(
            data => {
                if(data) {
                    this.profileList = data;
                } else {
                    this.profileList = null;
                }
                this.emails='';
                this.showEmailIdsModal();

                ////console.log('this.getForm', this.formData);
            },
            error => {
                //console.log('getAlsoKnownAsFormError', error);
            } 
        );
    };


    showEmailIdsModal(): void {
        /*
        $.colorbox({
            html : $compile($('#email-ids-modal').html())($scope),
            scrolling: true,
            onLoad: function() {
                $('#cboxClose').remove();
            }
        });

        setTimeout(function(){$.colorbox.resize({width:"575px"});},100);
        */
    };

    toggleSection(): void {
        this.showSection = !this.showSection;
        $('#find_ids_section').toggle();
    };
    
    /*
    
    */

    //Default init functions provided by Angular Core
    ngAfterViewInit() {
        //Fire functions AFTER the view inited. Useful when DOM is required or access children directives
    };

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    };

    ngOnInit() {
    }; 
}