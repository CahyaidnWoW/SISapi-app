<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class LivestockBreed extends Model
{
    protected $guarded = ['id'];

    public function livestocks() {
        return $this->hasMany(Livestock::class, 'breed_id');
    }
}
