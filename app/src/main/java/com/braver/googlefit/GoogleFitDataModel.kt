package com.braver.googlefit

class GoogleFitDataModel {
    var moduleName: String = ""
        get() = field
        set(value) {
            field = value
        }
    var createdDate: String = ""
        get() = field
        set(value) {
            field = value
        }
    var dataTypeOne: String = ""
        get() = field
        set(value) {
            field = value
        }

    var dataTypeTwo: String = ""
        get() = field
        set(value) {
            field = value
        }
    var dataTypeThree: String = ""
        get() = field
        set(value) {
            field = value
        }
    var dataTypeFour: String = ""
        get() = field
        set(value) {
            field = value
        }
    var dataValueOne: String = ""
        get() = field
        set(value) {
            field = value
        }
    var dataValueTwo: String = ""
        get() = field
        set(value) {
            field = value
        }
    var dataValueThree: String = ""
        get() = field
        set(value) {
            field = value
        }
    var dataValueFour: String = ""
        get() = field
        set(value) {
            field = value
        }
    var dataOneMeasure: String = ""
        get() = field
        set(value) {
            field = value
        }
    var dataTwoMeasure: String = ""
        get() = field
        set(value) {
            field = value
        }
    var dataThreeMeasure: String = ""
        get() = field
        set(value) {
            field = value
        }
    var dataFourMeasure: String = ""
        get() = field
        set(value) {
            field = value
        }

    constructor()
    constructor(
        moduleName: String,
        createdDate: String,
        dataTypeOne: String,
        dataTypeTwo: String,
        dataTypeThree: String,
        dataTypeFour: String,
        dataValueOne: String,
        dataValueTwo: String,
        dataValueThree: String,
        dataValueFour: String,
        dataOneMeasure: String,
        dataTwoMeasure: String,
        dataThreeMeasure: String,
        dataFourMeasure: String
    ) {
        this.moduleName = moduleName
        this.createdDate = createdDate
        this.dataTypeOne = dataTypeOne
        this.dataTypeTwo = dataTypeTwo
        this.dataTypeThree = dataTypeThree
        this.dataTypeFour = dataTypeFour
        this.dataValueOne = dataValueOne
        this.dataValueTwo = dataValueTwo
        this.dataValueThree = dataValueThree
        this.dataValueFour = dataValueFour
        this.dataOneMeasure = dataOneMeasure
        this.dataTwoMeasure = dataTwoMeasure
        this.dataThreeMeasure = dataThreeMeasure
        this.dataFourMeasure = dataFourMeasure
    }


}