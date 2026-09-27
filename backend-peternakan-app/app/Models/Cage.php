<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Cage extends Model
{
    protected $guarded = ['id'];

    public function livestocks() {
        return $this->hasMany(Livestock::class);
    }

    public function feedConsumptions() {
        return $this->hasMany(FeedConsumption::class);
    }
}
