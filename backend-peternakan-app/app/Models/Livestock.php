<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Livestock extends Model
{
    protected $guarded = ['id'];

    public function cage() {
        return $this->belongsTo(Cage::class);
    }

    public function breed() {
        return $this->belongsTo(LivestockBreed::class, 'breed_id');
    }

    public function weightLogs() {
        return $this->hasMany(WeightLog::class);
    }

    public function healthRecords() {
        return $this->hasMany(HealthRecord::class);
    }
}
