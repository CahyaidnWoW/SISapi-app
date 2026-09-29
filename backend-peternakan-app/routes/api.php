<?php

use App\Http\Controllers\Api\AuthController;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\CageController;
use App\Http\Controllers\Api\FeedStockController;
use App\Http\Controllers\Api\LivestockBreedController;
use App\Http\Controllers\Api\LivestockController;
use App\Http\Controllers\Api\FeedConsumptionController;
use App\Http\Controllers\Api\HealthRecordController;
use App\Http\Controllers\Api\WeightLogController;
use App\Http\Controllers\Api\FcrController;

Route::post('/login', [AuthController::class, 'login']);

Route::middleware('auth:sanctum')->group(function () {
    Route::get('/me', [AuthController::class, 'me']);
    Route::post('/logout', [AuthController::class, 'logout']);

    Route::get('/cages', [CageController::class, 'index']);
    Route::post('/cages', [CageController::class, 'store']);
    Route::get('/cages/{id}', [CageController::class, 'show']);
    Route::put('/cages/{id}', [CageController::class, 'update']);
    Route::delete('/cages/{id}', [CageController::class, 'destroy']);

    Route::get('/breeds', [LivestockBreedController::class, 'index']);
    Route::post('/breeds', [LivestockBreedController::class, 'store']);

    Route::get('/feeds', [FeedStockController::class, 'index']);
    route::post('/feeds', [FeedStockController::class, 'store']);
    Route::put('/feeds/{id}', [FeedStockController::class, 'update']);
    Route::delete('/feeds/{id}', [FeedStockController::class, 'destroy']);
    Route::post('/feeds/batches', [FeedStockController::class, 'addBatch']);

    Route::get('/livestocks', [LivestockController::class, 'index']);
    Route::post('/livestocks', [LivestockController::class, 'store']);
    route::patch('/livestocks/{id}', [LivestockController::class, 'update']);
    route::delete('/livestocks/{id}', [LivestockController::class, 'destroy']);
    Route::get('/livestocks/{tagNumber}', [LivestockController::class, 'showByTag']);

    Route::post('/cages/{id}/feed', [FeedConsumptionController::class, 'store']);
    

    Route::post('/livestocks/{tagNumber}/health-report', [HealthRecordController::class, 'store']);
    Route::get('/livestocks/{tagNumber}/health-records', [HealthRecordController::class, 'byLivestock']);
    Route::patch('/health-records/{id}', [HealthRecordController::class, 'update']);
    Route::get('/health-records/due', [HealthRecordController::class, 'due']);

    Route::post('/livestocks/{tagNumber}/weight-logs', [WeightLogController::class, 'storeByTag']);

    Route::get('/cages/{id}/fcr', [FcrController::class, 'getCageFcr']);
});